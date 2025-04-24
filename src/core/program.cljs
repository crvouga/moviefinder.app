(ns core.program
  (:require
   [clojure.core.async :as async]
   [clojure.pprint :as pprint]))

(def ^:private state! (atom {}))


(def ^:private msg-chan! (async/chan))
(def ^:private msg-mult! (async/mult msg-chan!))

(def ^:private eff-fns! (atom {}))
(def ^:private reducer-fns! (atom {}))


(defn put! [msg]
  (let [state-prev @state!
        reducer-fn (get @reducer-fns! (first msg) (constantly state-prev))
        state-new (reducer-fn state-prev msg)
        _ (reset! state! state-new)]
    (pprint/pprint {:put! msg}))
  (async/put! msg-chan! msg))

(defn take! [msg-type]
  (let [ch (async/chan)]
    (async/tap msg-mult! ch)
    (async/go-loop []
      (when-let [msg (async/<! ch)]
        (if (or (= msg-type :*)
                (= (first msg) msg-type))
          (do
            (async/close! ch)
            msg)
          (recur))))))

(defn read! []
  @state!)

(defn reg-reducer [msg-type reducer-fn]
  (pprint/pprint {:reg-reducer msg-type})
  (swap! reducer-fns! assoc msg-type reducer-fn))

(defn eff! [msg]
  (pprint/pprint {:eff msg})
  (let [maybe-eff-fn! (get @eff-fns! (first msg))
        eff-fn! (or maybe-eff-fn! (constantly nil))]
    (eff-fn! msg)))

(defn reg-eff [eff-type eff-fn]
  (pprint/pprint {:reg-eff eff-type})
  (swap! eff-fns! assoc eff-type eff-fn))
