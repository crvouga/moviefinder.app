(ns lib.program
  (:require
   [clojure.core.async :as a]
   [clojure.pprint :as pprint]
   [clojure.spec.alpha :as s]))

(s/def ::state! (s/and #(instance? js/Atom %) #(map? @%)))
(s/def ::msg-chan! #(instance? js/ManyToManyChannel %))
(s/def ::msg-mult! #(instance? js/Mult %))
(s/def ::eff-fns! (s/and #(instance? js/Atom %) #(map? @%)))
(s/def ::reducer-fns! (s/and #(instance? js/Atom %) #(map? @%)))

(s/def ::program
  (s/keys :req-un [::state!
                   ::msg-chan!
                   ::msg-mult!
                   ::eff-fns!
                   ::reducer-fns!]))

(s/def ::msg
  (s/or :simple-msg (s/cat :type keyword?)
        :complex-msg (s/cat :type keyword? :payload map?)))


(defn program? [input]
  (s/valid? ::program input))


(defn new
  "New is a function that takes no arguments. It returns a program. new"
  []
  (let [state! (atom {})
        msg-chan! (a/chan)
        msg-mult! (a/mult msg-chan!)
        eff-fns! (atom {})
        reducer-fns! (atom {})
        program {:program/state! state!
                 :program/msg-chan! msg-chan!
                 :program/msg-mult! msg-mult!
                 :program/eff-fns! eff-fns!
                 :program/reducer-fns! reducer-fns!}]
    program))



(defn- reducer
  "Reducer is a function that takes a program, a state, and a message. It returns a state. new"
  [program state msg]
  (let [{:keys [program/reducer-fns!]} program
        reducer-fn (get @reducer-fns! (first msg) (constantly state))]
    (reducer-fn state msg)))

(s/fdef reducer
  :args (s/cat :program ::program :state map? :msg ::msg)
  :ret map?)

(defn reg-reducer
  "Reg-reducer is a function that takes a program, a message type, and a reducer function. It returns a program. new"
  [program msg-type reducer-fn]
  (pprint/pprint {:reg-reducer msg-type})
  (let [{:keys [program/reducer-fns!]} program]
    (swap! reducer-fns! assoc msg-type reducer-fn)))


(s/fdef state!
  :args (s/cat :program ::program)
  :ret ::state!)

(defn state! [program]
  (let [{:keys [program/state!]} program]
    @state!))


(s/fdef eff!
  :args (s/cat :program ::program :eff ::msg)
  :ret map?)

(defn eff!
  "Eff is a function that takes a program, and a message. It returns a state. new"
  [program eff]
  (pprint/pprint {:eff eff})
  (let [{:keys [program/eff-fns!]} program
        maybe-eff-fn! (get @eff-fns! (first eff))
        eff-fn! (or maybe-eff-fn! (constantly nil))]
    (eff-fn! eff)))

(s/fdef reg-eff
  :args (s/cat :program ::program :eff-type keyword? :eff-fn fn?)
  :ret ::program)

(defn reg-eff
  "Reg-eff is a function that takes a program, a message type, and an effect function. It returns a program. new"
  [program eff-type eff-fn]
  (pprint/pprint {:reg-eff eff-type})
  (let [{:keys [program/eff-fns!]} program]
    (swap! eff-fns! assoc eff-type eff-fn)
    program))


(s/fdef put!
  :args (s/cat :program ::program :msg ::msg)
  :ret ::program)

(defn put!
  "Put is a function that takes a program, and a message. It returns a program. new"
  [program msg]
  (let [{:keys [program/msg-chan!
                program/state!]} program

        state-new (reducer program @state! msg)

        _ (reset! state! state-new)]
    (pprint/pprint {:put! (if (> (count (str msg)) 1000)
                            (str (first msg) " ... (truncated)")
                            msg)})
    (a/put! msg-chan! msg)
    program))

(s/fdef take!
  :args (s/cat :program ::program :msg-type keyword?)
  :ret ::msg)

(defn take!
  "Take is a function that takes a program, and a message type. It returns a message. new"
  [program msg-type]
  #_(assert (program? program) "Program must satisfy program spec")
  #_(assert (keyword? msg-type) "msg-type must be a keyword")

  (let [{:keys [program/msg-mult!]} program
        ch (a/chan)]
    (a/tap msg-mult! ch)
    (a/go-loop [ch ch]
      (when-let [msg (a/<! ch)]
        (if (or (= msg-type :*)
                (= (first msg) msg-type))
          (do
            (a/close! ch)
            msg)
          (recur ch))))))

(s/fdef take-every!
  :args (s/cat :program ::program :msg-type keyword? :f fn?)
  :ret ::program)

(defn take-every!
  "Take-every is a function that takes a program, a message type, and a function. It returns a program. new"
  [program msg-type f]
  (pprint/pprint {:take-every! msg-type})
  (a/go-loop []
    (let [msg (a/<! (take! program msg-type))]
      (f msg)
      (recur))))


(defn take-latest!
  "Take-latest is a function that takes a program, and a message type. It returns a message. new"
  [program msg-type f]
  (let [last-task (atom nil)]
    (a/go-loop []
      (let [msg (a/<! (take! program msg-type))
            task (a/go (f msg))]
        (when @last-task
          (a/close! @last-task))
        (reset! last-task task)
        (recur)))))