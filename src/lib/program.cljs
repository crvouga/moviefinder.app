(ns lib.program
  (:require
   [clojure.core.async :as a]
   [clojure.pprint :as pprint]))




(defn new
  "New is a function that takes no arguments. It returns a program. new"
  []
  (let [state! (atom {})
        msg-chan! (a/chan (a/sliding-buffer 10))
        msg-mult! (a/mult msg-chan!)
        eff-fns! (atom {})
        reducer-fns! (atom {})
        program {:program/state! state!
                 :program/msg-chan! msg-chan!
                 :program/msg-mult! msg-mult!
                 :program/eff-fns! eff-fns!
                 :program/reducer-fns! reducer-fns!}]

    program))

(defn- reduce-reducers [reducers state msg]
  (reduce
   (fn [state reducer-fn] (reducer-fn state msg))
   state
   reducers))

(defn- reducer
  "Reducer is a function that takes a program, a state, and a message. It returns a state. new"
  [program state [msg-type :as msg]]
  (let [{:keys [program/reducer-fns!]} program
        reducer-fns (get @reducer-fns! msg-type [])]
    (reduce-reducers reducer-fns state msg)))

(defn reg-reducer
  "Reg-reducer is a function that takes a program, a message type, and a reducer function. It returns a program. new"
  [program msg-type reducer-fn]
  #_(pprint/pprint {:reg-reducer msg-type})
  (let [{:keys [program/reducer-fns!]} program]
    (swap! reducer-fns! update msg-type (fnil conj []) reducer-fn)))

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
    (swap! eff-fns! assoc eff-type eff-fn)
    program))


(defn- msg->str [msg]
  (if (> (count (str msg)) 10000) (str (first msg) " ... (truncated)") msg))

(defn- update-state! [{:keys [program/state!] :as program} msg]
  (swap! state! #(reducer program % msg)))

(defn put!
  "Put is a function that takes a program, and a message. It returns a program. new"
  [{:keys [program/msg-chan!] :as program} msg]
  (update-state! program msg)
  (pprint/pprint {:put! (msg->str msg)})
  (a/put! msg-chan! msg)
  program)

(defn- new-msg-chan [{:keys [program/msg-mult!]}]
  (let [ch (a/chan)]
    (a/tap msg-mult! ch)
    ch))

(defn msg-match? [msg msg-type]
  (or (= msg-type :*)
      (= (first msg) msg-type)))

(defn take!
  "Take is a function that takes a program, and a message type. It returns a message. new"
  [program msg-type]
  #_(pprint/pprint {:take! msg-type})
  (let [ch (new-msg-chan program)]
    (a/go-loop []
      (let [msg (a/<! ch)]
        (when-not (msg-match? msg msg-type)
          (recur))
        (a/close! ch)
        msg))))


(defn take-every!
  "Take-every is a function that takes a program, a message type, and a function. It returns a program. new"
  [program msg-type f]
  #_(pprint/pprint {:take-every! msg-type})
  (a/go-loop []
    (let [msg (a/<! (take! program msg-type))]
      (f msg)
      (recur))))

