(ns lib.program-test
  (:require
   [cljs.test :refer-macros [deftest is testing]]
   [lib.program :as p]
   [clojure.core.async :as a]))

(deftest new-program-test
  (testing "Creates a new program instance"
    (let [program (p/new)]
      (is (map? program))
      (is (contains? program :program/state!))
      (is (contains? program :program/eff-fns!))
      (is (contains? program :program/reducer-fns!))
      (is (contains? program :program/listener-fns!))
      (is (empty? (p/state! program)))
      (is (empty? @(:program/eff-fns! program)))
      (is (empty? @(:program/reducer-fns! program)))
      (is (empty? @(:program/listener-fns! program))))))

(deftest reducer-test
  (testing "Reducers update state correctly"
    (let [program (p/new)]
      (p/reg-reducer program :set (fn [state [_ k v]] (assoc state k v)))
      (p/reg-reducer program :inc (fn [state [_ k]] (update state k (fnil inc 0))))
      (p/reg-reducer program :inc (fn [state [_ k]] (update state (keyword (str (name k) "-doubled")) (fnil #(* % 2) 0)))) ; Test multiple reducers

      (is (empty? (p/state! program)) "Initial state is empty")

      (p/put! program [:set :a 10])
      (is (= {:a 10} (p/state! program)) "State updated by :set reducer")

      (p/put! program [:inc :a])
      (is (= {:a 11 :a-doubled 0} (p/state! program)) "State updated by :inc reducers")

      (p/put! program [:inc :a])
      (is (= {:a 12 :a-doubled 0} (p/state! program)) "State updated again by :inc reducers")

      (p/put! program [:set :b 5])
      (is (= {:a 12 :a-doubled 0 :b 5} (p/state! program)) "State updated by another :set")

      (p/put! program [:unknown-msg])
      (is (= {:a 12 :a-doubled 0 :b 5} (p/state! program)) "State unchanged by unknown message"))))

(deftest effect-test
  (testing "Effects execute correctly"
    (let [program (p/new)
          effect-result (atom nil)]
      (p/reg-eff program :sync-effect (fn [[_ data]] (reset! effect-result data) :sync-done))
      (p/reg-eff program :async-effect (fn [[_ data]] (a/go (reset! effect-result data) :async-done)))

      (testing "Synchronous effect"
        (let [res (p/eff! program [:sync-effect {:val 1}])]
          (is (= :sync-done res))
          (is (= {:val 1} @effect-result))))

      #_(testing "Asynchronous effect"
          (async done
                 (let [res-chan (p/eff! program [:async-effect {:val 2}])]
                   (is (satisfies? a/ReadPort res-chan) "Effect returns a channel")
                   (let [res (a/<! res-chan)]
                     (is (= :async-done res))
                     (is (= {:val 2} @effect-result))
                     (done)))))

      (testing "Unregistered effect"
        ;; TODO: How to reliably test js/console.warn? For now, just check return value.
        (let [res (p/eff! program [:unregistered-effect])]
          (is (nil? res)))))))

#_(deftest take!-test
    (testing "take! receives the next message"
      (let [program (p/new)]
        (async
         done
         (a/go
           (let [msg1 [:data 1]
                 msg2 [:data 2]
                 msg3 [:other 3]
                 data-chan (p/take! program :data)
                 other-chan (p/take! program :other)
                 any-chan (p/take! program :*)] ; Takes the first message put! after registration

             (p/put! program msg1)
             (p/put! program msg3)
             (p/put! program msg2) ; This should not be received by data-chan

             (is (= msg1 (a/<! data-chan)) "Receives the first matching message")
             (is (= msg3 (a/<! other-chan)) "Receives the first matching message for its type")
             (is (= msg1 (a/<! any-chan)) "Receives the first message overall")

             ;; Check that channels are closed and listeners removed
             (let [[_ port] (a/alts! [data-chan (a/timeout 10)])]
               (is (not= data-chan port) "Channel should be closed or empty after receiving"))
             (let [[_ port] (a/alts! [other-chan (a/timeout 10)])]
               (is (not= other-chan port) "Channel should be closed or empty after receiving"))
             (let [[_ port] (a/alts! [any-chan (a/timeout 10)])]
               (is (not= any-chan port) "Channel should be closed or empty after receiving"))

             (is (= 0 (count @(:program/listener-fns! program))) "All take! listeners should be removed")

             (done)))))))

(deftest take-every!-test
  (testing "take-every! receives all matching messages"
    (a/go
      (let [program (p/new)
            received-data (atom [])
            received-any (atom [])]

        (p/take-every! program :data (fn [msg] (swap! received-data conj msg)))
        (p/take-every! program :* (fn [msg] (swap! received-any conj msg)))

        (let [msg1 [:data 1]
              msg2 [:other 2]
              msg3 [:data 3]]
          (p/put! program msg1)
          (p/put! program msg2)
          (p/put! program msg3)

          ;; Allow potential async operations in listeners to settle (though they are sync here)
          (a/<! (a/timeout 10))

          (is (= [msg1 msg3] @received-data) "Receives all :data messages")
          (is (= [msg1 msg2 msg3] @received-any) "Receives all messages")

          (is (= 2 (count @(:program/listener-fns! program))) "take-every! listeners should persist"))))))

(deftest combined-reducer-listener-test
  (testing "put! triggers both reducers and listeners"
    (a/go
      (let [program (p/new)
            listener-received (atom nil)]
        (p/reg-reducer program :update (fn [state [_ val]] (assoc state :value val)))
        (p/take-every! program :update (fn [msg] (reset! listener-received msg)))

        (let [msg [:update "hello"]]
          (p/put! program msg)

          (a/<! (a/timeout 10)) ; Allow listener to process

          (is (= {:value "hello"} (p/state! program)) "Reducer updated the state")
          (is (= msg @listener-received) "Listener received the message"))))))

(deftest msg-match?-test
  (testing "msg-match? correctly identifies matching messages"
    (is (p/msg-match? [:event 1] :event))
    (is (not (p/msg-match? [:event 1] :other-event)))
    (is (p/msg-match? [:event 1] :*))
    (is (p/msg-match? [:other-event] :*))
    (is (not (p/msg-match? [] :event))) ; Edge case: empty message
    (is (p/msg-match? [] :*)))) ; Edge case: empty message with wildcard
