(ns core.program
  (:require
   [clojure.core.async :as async]))

(defprotocol Program
  (put! [this msg])
  (take! [this msg-type])
  (read! [this])
  (start! [this]))

(defmulti eff! (fn [_ msg] (first msg)))
(defmulti reducer (fn [_ msg] (first msg)))


(deftype ProgramInstance [sagas! state! state-chan! state-mult! msg-chan! msg-mult!]
  Program
  (eff! [_ msg-type]
    (eff! msg-type))

  (put! [_ msg]
    (println "put!" msg)
    (async/put! msg-chan! msg))

  (take! [_ msg-type]
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

  (read! [_]
    @state!)

  (extend! [_ & sagas]
    (println "register!" sagas)
    (swap! sagas! into sagas))

  (start! [this]
    (async/go-loop []
      (when-let [msg (async/<! (.take! this :*))]
        (println "msg" msg)
        (println "state" (.read! this))
        (recur)))

    (doseq [saga @sagas!]
      (println "start!" saga)
      (saga this))))

(defn create []
  (let [sagas! (atom #{})
        state! (atom {})
        state-chan! (async/chan)
        state-mult! (async/mult state-chan!)
        msg-chan! (async/chan)
        msg-mult! (async/mult msg-chan!)]

    (->ProgramInstance sagas! state! state-chan! state-mult! msg-chan! msg-mult!)))