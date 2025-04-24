(ns core.program
  (:require
   [clojure.core.async :as async]))

(def ^:private state! (atom {}))
(def ^:private msg-chan! (async/chan))
(def ^:private msg-mult! (async/mult msg-chan!))
(def ^:private eff-handlers! (atom {}))
(def ^:private reducers! (atom {}))


(defn put! [msg]
  (println "put!" msg)
  (let [state-prev @state!
        reducer-fn (get @reducers! (constantly state-prev))
        state-new (reducer-fn state-prev msg)
        _ (reset! state! state-new)]
    (println "reducer" reducer-fn "state-prev" state-prev "state-new" state-new))
  (async/put! msg-chan! msg))

(defn take! [msg-type]
  (println "register take!" msg-type)
  (let [ch (async/chan)]
    (async/tap msg-mult! ch)
    (async/go-loop []
      (when-let [msg (async/<! ch)]
        (if (or (= msg-type :*)
                (= (first msg) msg-type))
          (do
            (println "take!" msg)
            (async/close! ch)
            msg)
          (recur))))))

(defn read! []
  @state!)

(defn eff! [eff-type]
  (let [eff-fn (get @eff-handlers! eff-type)]
    (println "eff!" eff-fn)
    (eff-fn)))

(defn reg-eff [eff-type eff-fn]
  (swap! eff-handlers! assoc eff-type eff-fn))

(defn reg-reducer [msg-type reducer-fn]
  (swap! reducers! assoc msg-type reducer-fn))
