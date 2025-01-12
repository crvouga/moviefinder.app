(ns linkhub.frontend.store
  (:require [reagent.core :as r]
            [cljs.pprint]))

(defonce state! (r/atom {}))
(def inits! (r/atom #{}))
(def steps! (r/atom #{}))

(defmulti effect! (fn [i] (-> i :store/effects first)))

(defn- reducer [acc step-fn] 
    (let [stepped (step-fn (assoc acc :store/effects []))
          state-new (merge (:store/state acc) (:store/state stepped))
          effect-new (concat (:store/effects acc) (:store/effects stepped))]
      {:store/event (-> stepped :store/event)
       :store/state state-new
       :store/effects effect-new}))

(defn- dispatch! [event]
  (let [state-prev @state!
        initial {:store/event event 
                 :store/state state-prev
                 :store/effects []}
        stepped (reduce reducer initial @steps!)
        state-new (:store/state stepped)
        effects (:store/effects stepped)]
    (cljs.pprint/pprint {:event event 
                         :state-prev state-prev 
                         :state-new state-new 
                         :effects effects})
    (doseq [effect effects]
      (effect! {:store/effects effect
                :store/state state-new
                :store/dispatch! dispatch!}))
    (reset! state! state-new)))

(defn init! []
  (let [init-fns @inits!
        values (map (fn [init-fn] (init-fn)) init-fns)
        states (map :store/state values)
        events (mapcat :store/events values)
        effects (mapcat :store/effects values)
        state (reduce merge states)]
    (cljs.pprint/pprint {:state state
                         :events events
                         :effects effects})
    (reset! state! state)
    (doseq [event events]
      (dispatch! event))
    (doseq [effect effects]
      (effect! {:store/effects effect
                :store/state state
                :store/dispatch! dispatch!}))))

(defn register! [module] 
  (let [step (:store/step module)
        init (:store/init module)]
    (when init
      (swap! inits! conj init))
    (when step
      (swap! steps! conj step))))


(defn view [view-fn]
  (let [i {:store/state @state!
           :store/dispatch! dispatch!}]
    (view-fn i)))

(defn event [input]
  (-> input :store/event))

(defn event-type [input]
  (-> input event first))

(defn event-payload [input]
  (-> input event second))