(ns linkpage.frontend.store
  (:require [reagent.core :as r]
            [cljs.pprint]))

(defonce state! (r/atom {}))
(def inits! (atom #{}))
(def steps! (atom #{}))

(defn effect [i]
  (-> i :store/effect))

(defn effect-type [i]
  (-> i effect first))

(defn effect-payload [i]
  (-> i effect second))

(defmulti effect! effect-type)

(defn- reducer [acc step-fn]
  (let [stepped (step-fn (assoc acc :store/effects []))
        state-new (merge (:store/state acc) (:store/state stepped))
        effect-new (concat (:store/effects acc) (:store/effects stepped))]
    {:store/msg (-> stepped :store/msg)
     :store/state state-new
     :store/effects effect-new}))

(defn- -dispatch! [msg]
  (let [state-prev @state!
        initial {:store/msg msg
                 :store/state state-prev
                 :store/effects []}
        stepped (reduce reducer initial @steps!)
        state-new (:store/state stepped)
        effects (:store/effects stepped)]
    (cljs.pprint/pprint {:msg msg
                         :state-prev state-prev
                         :state-new state-new
                         :effects effects})
    (doseq [effect effects]
      (cljs.pprint/pprint {:effect effect})
      (effect! {:store/effect effect
                :store/state state-new
                :store/dispatch! -dispatch!}))
    (reset! state! state-new)))

(defn init! []
  (let [init-fns @inits!
        values (map (fn [init-fn] (init-fn)) init-fns)
        states (map :store/state values)
        msgs (mapcat :store/msgs values)
        effects (mapcat :store/effects values)
        state (reduce merge states)]
    (cljs.pprint/pprint {:state state
                         :msgs msgs
                         :effects effects})
    (reset! state! state)
    (doseq [msg msgs]
      (-dispatch! msg))
    (doseq [effect effects]
      (cljs.pprint/pprint {:effect effect})
      (effect! {:store/effect effect
                :store/state state
                :store/dispatch! -dispatch!}))))

(defn register! [module]
  (let [step (:store/step module)
        init (:store/init module)]
    (when init
      (swap! inits! conj init))
    (when step
      (swap! steps! conj step))))


(defn view [view-fn]
  (let [i {:store/state @state!
           :store/dispatch! -dispatch!}]
    (view-fn i)))

(defn dispatch! [i msg]
  ((-> i :store/dispatch!) msg))


(defn msg [i]
  (-> i :store/msg))

(defn msg-type [i]
  (-> i msg first))

(defn msg-payload [i]
  (-> i msg second))