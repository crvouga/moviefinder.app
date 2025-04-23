(ns app.frontend.store
  (:require [cljs.pprint]
            ["react-dom/client" :as rd]
            [reagent.core :as r]))

(defonce ^:private state! (atom {}))
(def ^:private transitions! (atom #{}))
(def ^:private msg-queue! (atom []))
(def ^:private processing-msg (atom false))
(def ^:private view-fn! (atom nil))
(def ^:private sagas! (atom []))
(def to-msg-type (comp first :store/msg))
(def to-msg-payload (comp second :store/msg))
(def to-eff-type (comp first :store/eff))
(def to-eff-payload (comp second :store/eff))

(defmulti eff! to-eff-type)

#_(defonce ^:private root
    (let [dom-root (.getElementById js/document "root")]
      (rd/createRoot dom-root)))

(defn- render! [put!]
  #_(when-let [view-fn @view-fn!]
      (.render root
               (r/as-element
                [view-fn {:store/state @state!
                          :store/put! put!}]))))

(defn- apply-transition [acc transition-fn]
  (let [msg (-> acc :store/msg)
        transitioned (transition-fn (assoc acc :store/effs [] :store/msgs []))
        state-new (merge (:store/state acc) (:store/state transitioned))
        eff-new (concat (:store/effs acc) (:store/effs transitioned))
        msgs-new (concat (:store/msgs acc) (:store/msgs transitioned))]
    {:store/msg msg
     :store/msgs msgs-new
     :store/state state-new
     :store/effs eff-new}))

(defn- process-msg! [msg]
  (let [state-prev @state!
        acc {:store/msg msg
             :store/state state-prev
             :store/effs []
             :store/msgs []}
        transitioned (reduce apply-transition acc @transitions!)
        state-new (-> transitioned :store/state)
        effs (->> transitioned :store/effs (filter vector?))
        msgs (->> transitioned :store/msgs (filter vector?))]
    #_(cljs.pprint/pprint {:msg msg
                           :effs effs
                           :msgs msgs})
    (reset! state! state-new)
    (render! process-msg!)

    (doseq [msg msgs]
      (if @processing-msg
        (swap! msg-queue! conj msg)
        (process-msg! msg)))

    (doseq [eff effs]
      (eff! {:store/eff eff
             :store/state! state!
             :store/put! process-msg!}))))

(defn- process-queued-msgs! []
  (when-let [queued-msgs (seq @msg-queue!)]
    (reset! msg-queue! [])
    (doseq [msg queued-msgs]
      (process-msg! msg))))

(defn put! [_ msg]
  (if @processing-msg
    (swap! msg-queue! conj msg)
    (do
      (reset! processing-msg true)
      (process-msg! msg)
      (process-queued-msgs!)
      (reset! processing-msg false))))

(defn register-eff!
  ([] nil)
  ([_] nil)
  ([eff-type- eff-handler & rest]
   (let [eff-handler-new (fn [i] (if (= eff-type- (to-eff-type i)) (eff-handler i) i))]
     (defmethod eff! eff-type- [i]
       (eff-handler-new i))
     (apply register-eff! rest))))

(defn register!
  ([] nil)
  ([_] nil)
  ([msg-type- transition & rest]
   (let [transition-new (fn [i] (if (= msg-type- (to-msg-type i)) (transition i) i))]
     (swap! transitions! conj transition-new)
     (apply register! rest))))



(defn initialize! [view-fn]
  (reset! view-fn! view-fn)
  (process-msg! [:store/initialized]))
