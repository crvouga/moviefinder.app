(ns moviefinder-app.frontend.store
  (:require [cljs.pprint]
            [clojure.core.async :as async :refer [chan go-loop <!]]
            [reagent.core :as r]))

(defonce ^:private state! (r/atom {}))
(def ^:private transitions! (atom #{}))
(def ^:private msg-chan! (chan))

(def msg-type (comp first :store/msg))
(def msg-payload (comp second :store/msg))
(def eff-type (comp first :store/eff))
(def eff-payload (comp second :store/eff))

(defn put! [i msg] ((-> i :store/put!) msg))



#_(defn register-transition! [transition] (swap! transitions! conj transition))

(defmulti eff! eff-type)

(defn reg-eff!
  ([] nil)
  ([_] nil)
  ([eff-type- eff-handler & rest]
   (let [eff-handler-new (fn [i] (if (= eff-type- (eff-type i)) (eff-handler i) i))]
     (defmethod eff! eff-type- [i]
       (eff-handler-new i))

     (apply reg-eff! rest))))

(defn reg!
  ([] nil)
  ([_] nil)
  ([msg-type- transition & rest]
   (let [transition-new (fn [i] (if (= msg-type- (msg-type i)) (transition i) i))]
     (swap! transitions! conj transition-new)
     (apply reg! rest))))

(defn initialize! []
  (async/put! msg-chan! [:store/initialized]))

(defn view [view-fn]
  (let [i {:store/state @state!
           :store/put! #(async/put! msg-chan! %)}]
    (view-fn i)))

(defn- transition-reducer [acc transition-fn]
  (let [msg (-> acc :store/msg)
        transitioned (transition-fn (assoc acc :store/effs [] :store/msgs []))
        state-new (merge (:store/state acc) (:store/state transitioned))
        eff-new (concat (:store/effs acc) (:store/effs transitioned))
        msgs-new (concat (:store/msgs acc) (:store/msgs transitioned))]
    {:store/msg msg
     :store/msgs msgs-new
     :store/state state-new
     :store/effs eff-new}))

(defn- transition! [msg]
  (let [state-prev @state!
        acc {:store/msg msg
             :store/state state-prev
             :store/effs []
             :store/msgs []}
        transitioned (reduce transition-reducer acc @transitions!)
        state-new (-> transitioned :store/state)
        effs (->> transitioned :store/effs (filter vector?))
        msgs (->> transitioned :store/msgs (filter vector?))]
    (cljs.pprint/pprint {:msg msg
                         :state-new state-new
                         :effs effs
                         :msgs msgs})
    (reset! state! state-new)
    (doseq [msg msgs]
      (async/put! msg-chan! msg))
    (doseq [eff effs]
      (eff! {:store/eff eff
             :store/state! state!
             :store/put! #(async/put! msg-chan! %)}))))

(go-loop []
  (let [msg (<! msg-chan!)]
    (transition! msg)
    (recur)))