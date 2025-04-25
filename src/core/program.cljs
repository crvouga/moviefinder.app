(ns core.program
  (:require
   [clojure.core.async :as a]
   [clojure.pprint :as pprint]))

(defn new
  "New is a function that takes no arguments. It returns a program. new"
  []
  (let [state! (atom {})
        msg-chan! (a/chan)
        msg-mult! (a/mult msg-chan!)
        eff-fns! (atom {})
        reducer-fns! (atom {})]
    {:program/state! state!
     :program/msg-chan! msg-chan!
     :program/msg-mult! msg-mult!
     :program/eff-fns! eff-fns!
     :program/reducer-fns! reducer-fns!}))

(defn program? [input]
  (and (map? input)
       (:program/state! input)
       (:program/msg-chan! input)
       (:program/msg-mult! input)
       (:program/eff-fns! input)
       (:program/reducer-fns! input)))


(defn reducer
  "Reducer is a function that takes a program, a state, and a message. It returns a state. new"
  [program state msg]
  (let [{:keys [program/reducer-fns!]} program
        reducer-fn (get @reducer-fns! (first msg) (constantly state))]
    (reducer-fn state msg)))

(defn reg-reducer
  "Reg-reducer is a function that takes a program, a message type, and a reducer function. It returns a program. new"
  [program msg-type reducer-fn]
  (pprint/pprint {:reg-reducer msg-type})
  (let [{:keys [program/reducer-fns!]} program]
    (swap! reducer-fns! assoc msg-type reducer-fn)))


(defn state! [program]
  (let [{:keys [program/state!]} program]
    @state!))

(defn eff!
  "Eff is a function that takes a program, and a message. It returns a state. new"
  [program eff]
  (pprint/pprint {:eff eff})
  (let [{:keys [program/eff-fns!]} program
        maybe-eff-fn! (get @eff-fns! (first eff))
        eff-fn! (or maybe-eff-fn! (constantly nil))]
    (eff-fn! eff)))

(defn reg-eff
  "Reg-eff is a function that takes a program, a message type, and an effect function. It returns a program. new"
  [program eff-type eff-fn]
  (pprint/pprint {:reg-eff eff-type})
  (let [{:keys [program/eff-fns!]} program]
    (swap! eff-fns! assoc eff-type eff-fn)))


(defn put!
  "Put is a function that takes a program, and a message. It returns a program. new"
  [program msg]
  (let [{:keys [program/msg-chan!
                program/state!]} program

        state-new (reducer program @state! msg)

        _ (reset! state! state-new)]
    (pprint/pprint {:put! msg})
    (a/put! msg-chan! msg)))

(defn take!
  "Take is a function that takes a program, and a message type. It returns a message. new"
  [program msg-type]

  (when (not (program? program))
    (throw (js/Error. (str "program must be a program: " program))))

  (when (not (keyword? msg-type))
    (throw (js/Error. (str "msg-type must be a keyword: " msg-type))))

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


(defn take-every!
  "Take-every is a function that takes a program, a message type, and a function. It returns a program. new"
  [program msg-type f]
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