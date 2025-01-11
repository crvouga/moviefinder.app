(ns linkhub.frontend.store
  (:require [reagent.core :as r]
            [cljs.pprint]))

(defonce state! (r/atom {}))
(defonce inits! (r/atom #{}))
(defonce steps! (r/atom #{}))

(defn- reducer [acc step-fn] 
    (let [stepped (step-fn acc)
          state-new (merge (:store/state acc) (:store/state stepped))
          effect-new (concat (:store/effect acc) (:store/effect stepped))]
      {:store/event (-> stepped :store/event)
       :store/state state-new
       :store/effect effect-new}))

(defmulti effect! (fn [i] (-> i :store/effect first)))

(defn dispatch! [event]
  (let [state-prev @state!
        initial {:store/event event 
                 :store/state state-prev
                 :store/effect []}
        stepped (reduce reducer initial @steps!)
        state-new (:store/state stepped)
        effects (:store/effect stepped)]
    (cljs.pprint/pprint {:event event 
                         :state-prev state-prev 
                         :state-new state-new 
                         :effects effects})
    (doseq [effect effects]
      (effect! {:store/effect effect
                :store/state state-new
                :store/dispatch! dispatch!}))
    (reset! state! state-new)))

(defn init! []
  (let [init-fns @inits!
        values (map (fn [init-fn] (init-fn)) init-fns)
        states (map :store/state values)
        events (mapcat :store/events values)
        state (reduce merge states)]
    #_(cljs.pprint/pprint {:events events 
                         :state state})
    (reset! state! state)
    (doseq [event events]
      (dispatch! event))))

(defn register! [module] 
  (let [step (:store/step module)
        init (:store/init module)]
    (when init
      (swap! inits! conj init))
    (when step
      (swap! steps! conj step))))


(defn event [input]
  (-> input :store/event))

(defn event-type [input]
  (-> input event first))

(defn event-payload [input]
  (-> input event second))