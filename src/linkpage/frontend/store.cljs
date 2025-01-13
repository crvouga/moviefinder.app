(ns linkpage.frontend.store
  (:require [cljs.pprint]
            [clojure.core.async :refer [chan put! go-loop <!]]
            [reagent.core :as r]))

(defonce ^:private state! (r/atom {}))
(def ^:private steps! (atom #{}))
(def ^:private msg-chan! (chan))

(def msg-type (comp first :store/msg))
(def msg-payload (comp second :store/msg))
(def eff-type (comp first :store/eff))
(def eff-payload (comp second :store/eff))

(defn dispatch! [i msg] ((-> i :store/dispatch!) msg))

(defmulti eff! eff-type)

(defn register-step! [step] (swap! steps! conj step))

(defn initialize! []
  (put! msg-chan! [:store/initialized]))

(defn view [view-fn]
  (let [i {:store/state @state!
           :store/dispatch! #(put! msg-chan! %)}]
    (view-fn i)))

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

(defn- step! [msg]
  (let [state-prev @state!
        acc {:store/msg msg
             :store/state state-prev
             :store/effs []
             :store/msgs []}
        stepped (reduce step-reducer acc @steps!)
        state-new (-> stepped :store/state)
        effs (->> stepped :store/effs (filter vector?))
        msgs (->> stepped :store/msgs (filter vector?))]
    (cljs.pprint/pprint {:msg msg
                         :state-new state-new
                         :effs effs
                         :msgs msgs})
    (reset! state! state-new)
    (doseq [msg msgs]
      (put! msg-chan! msg))
    (doseq [eff effs]
      (eff! {:store/eff eff
             :store/state! state!
             :store/dispatch! #(put! msg-chan! %)}))))

(go-loop []
  (let [msg (<! msg-chan!)]
    (step! msg)
    (recur)))