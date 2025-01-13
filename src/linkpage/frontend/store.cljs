(ns linkpage.frontend.store
  (:require [cljs.pprint]
            [clojure.core.async :refer [chan put! go <!]]
            [reagent.core :as r]))

(defonce ^:private state! (r/atom {}))
(def ^:private steps! (atom #{}))
(def ^:private msg-chan! (chan))

(defn register-step! [step]
  (swap! steps! conj step))

(def eff :store/eff)
(def eff-type (comp first eff))
(def eff-payload (comp second eff))

(defmulti eff! eff-type)

(def msg :store/msg)
(def msg-type (comp first msg))
(def msg-payload (comp second msg))

(defn- step-reducer [acc step-fn]
  (let [msg (-> acc :store/msg)
        stepped (step-fn (assoc acc :store/effs [] :store/msgs []))
        state-new (merge (:store/state acc) (:store/state stepped))
        eff-new (concat (:store/effs acc) (:store/effs stepped))
        msgs-new (concat (:store/msgs acc) (:store/msgs stepped))]
    {:store/msg msg
     :store/msgs msgs-new
     :store/state state-new
     :store/effs eff-new}))


(defn initialize! []
  (put! msg-chan! [:store/initialized]))

(defn view [view-fn]
  (let [i {:store/state @state!
           :store/dispatch! #(put! msg-chan! %)}]
    (view-fn i)))

(defn dispatch! [i msg]
  ((-> i :store/dispatch!) msg))


(defn- process-msg! [msg]
  (let [state-prev @state!
        initial {:store/msg msg
                 :store/state state-prev
                 :store/effs []
                 :store/msgs []}
        stepped (reduce step-reducer initial @steps!)
        state-new (-> stepped :store/state)
        effs (->> stepped :store/effs (filter vector?))
        msgs (->> stepped :store/msgs (filter vector?))]
    (cljs.pprint/pprint {:message "Dispatched msg"
                         :msg msg
                         :state-prev state-prev
                         :state-new state-new
                         :effs effs
                         :msgs msgs})
    (doseq [msg msgs]
      (put! msg-chan! msg))
    (doseq [eff effs]
      (cljs.pprint/pprint {:message "Running effect"
                           :eff eff})
      (eff! {:store/eff eff
             :store/state state-new
             :store/dispatch! #(put! msg-chan! %)}))
    (reset! state! state-new)))

(go
  (loop []
    (let [msg (<! msg-chan!)]
      (process-msg! msg)
      (recur))))