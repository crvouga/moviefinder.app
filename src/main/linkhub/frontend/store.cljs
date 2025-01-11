(ns linkhub.frontend.store
  (:require [reagent.core :as r]))

(defonce state! (r/atom {}))
(defonce inits! (r/atom #{}))
(defonce steps! (r/atom #{}))

(defn init! []
  (let [init-fns @inits!
        init-values (map (fn [init-fn] (init-fn)) init-fns)
        init-states (map :store/state init-values)
        state-new (reduce merge init-states)]
    (reset! state! state-new)))

(defn- reducer [acc step-fn] 
    (let [stepped (step-fn acc)
          state-new (merge (:store/state acc) (:store/state stepped))]
      {:store/event (-> stepped :store/event)
       :store/state state-new}))

(defn dispatch! [event]
  (let [initial {:store/event event 
                 :store/state @state!}
        stepped (reduce reducer initial @steps!)
        state-new (:store/state stepped)]
    (reset! state! state-new)))

(defn register! [module]
    (swap! inits! conj (:store/init module))
    (swap! steps! conj (:store/step module)))

