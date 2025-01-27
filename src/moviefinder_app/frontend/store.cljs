(ns moviefinder-app.frontend.store
  (:require [cljs.pprint]
            [reagent.core :as r]))

(defonce ^:private state! (r/atom {}))
(def ^:private transitions! (atom #{}))
(def ^:private msg-queue! (atom []))
(def ^:private processing-msg (atom false))

(def msg-type (comp first :store/msg))
(def msg-payload (comp second :store/msg))
(def eff-type (comp first :store/eff))
(def eff-payload (comp second :store/eff))

(defmulti eff! eff-type)

(defn- process-msg! [msg]
  (let [state-prev @state!
        acc {:store/msg msg
             :store/state state-prev
             :store/effs []
             :store/msgs []}
        transitioned (reduce
                      (fn [acc transition-fn]
                        (let [msg (-> acc :store/msg)
                              transitioned (transition-fn (assoc acc :store/effs [] :store/msgs []))
                              state-new (merge (:store/state acc) (:store/state transitioned))
                              eff-new (concat (:store/effs acc) (:store/effs transitioned))
                              msgs-new (concat (:store/msgs acc) (:store/msgs transitioned))]
                          {:store/msg msg
                           :store/msgs msgs-new
                           :store/state state-new
                           :store/effs eff-new}))
                      acc
                      @transitions!)
        state-new (-> transitioned :store/state)
        effs (->> transitioned :store/effs (filter vector?))
        msgs (->> transitioned :store/msgs (filter vector?))]
    (cljs.pprint/pprint {:msg msg
                         :effs effs
                         :msgs msgs})
    (reset! state! state-new)
    (doseq [msg msgs]
      (if @processing-msg
        (swap! msg-queue! conj msg)
        (process-msg!  msg)))
    (doseq [eff effs]
      (eff! {:store/eff eff
             :store/state! state!
             :store/put! process-msg!}))))

(defn put! [_ msg]
  (if @processing-msg
    (swap! msg-queue! conj msg)
    (do
      (reset! processing-msg true)
      (process-msg! msg)
      (when-let [queued-msgs (seq @msg-queue!)]
        (reset! msg-queue! [])
        (doseq [msg queued-msgs]
          (process-msg! msg)))
      (reset! processing-msg false))))

(defn register-eff!
  ([] nil)
  ([_] nil)
  ([eff-type- eff-handler & rest]
   (let [eff-handler-new (fn [i] (if (= eff-type- (eff-type i)) (eff-handler i) i))]
     (defmethod eff! eff-type- [i]
       (eff-handler-new i))
     (apply register-eff! rest))))

(defn register!
  ([] nil)
  ([_] nil)
  ([msg-type- transition & rest]
   (let [transition-new (fn [i] (if (= msg-type- (msg-type i)) (transition i) i))]
     (swap! transitions! conj transition-new)
     (apply register! rest))))

(defn initialize! []
  (put! nil [:store/initialized]))

(defn view [view-fn]
  (let [i {:store/state @state!
           :store/put! put!}]
    (view-fn i)))