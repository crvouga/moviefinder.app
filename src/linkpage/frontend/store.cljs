(ns linkpage.frontend.store
  (:require [cljs.pprint]
            [reagent.core :as r]))

(defonce ^:private state! (r/atom {}))
(def ^:private steps! (atom #{}))

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
  (let [stepped (step-fn (assoc acc :store/effs []))
        state-new (merge (:store/state acc) (:store/state stepped))
        eff-new (concat (:store/effs acc) (:store/effs stepped))]
    {:store/msg (-> stepped :store/msg)
     :store/msgs (-> stepped :store/msgs)
     :store/state state-new
     :store/effs eff-new}))

(defn- internal-dispatch! [msg]
  (let [state-prev @state!
        initial {:store/msg msg
                 :store/state state-prev
                 :store/effs []
                 :store/msgs []}
        stepped (reduce step-reducer initial @steps!)
        state-new (:store/state stepped)
        effs (:store/effs stepped)
        msgs (:store/msgs stepped)]
    (cljs.pprint/pprint {:message "Dispatched msg"
                         :msg msg
                         :state-prev state-prev
                         :state-new state-new
                         :effs effs
                         :msgs msgs})
    (doseq [msg msgs]
      (internal-dispatch! msg))
    (doseq [eff effs]
      (cljs.pprint/pprint {:message "Running effect"
                           :eff eff})
      (eff! {:store/eff eff
             :store/state state-new
             :store/dispatch! internal-dispatch!}))
    (reset! state! state-new)))

(defn initialize! []
  (internal-dispatch! [:store/initialized]))

(defn view [view-fn]
  (let [i {:store/state @state!
           :store/dispatch! internal-dispatch!}]
    (view-fn i)))

(defn dispatch! [i msg]
  ((-> i :store/dispatch!) msg))