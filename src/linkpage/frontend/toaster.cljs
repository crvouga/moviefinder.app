(ns linkpage.frontend.toaster
  (:require [linkpage.frontend.store :as store]
            [clojure.core.async :refer [go <! timeout]]))


(defmulti transition store/msg-type)

(store/register-transition! transition)

(defmethod transition :default [i] i)

(defmethod transition :store/initialized [i]
  (-> i
      (assoc-in [:store/state ::toasts] [])
      (assoc-in [:store/state ::running-id] 0)))

(defn assoc-running-id [toast i]
  (-> toast
      (assoc ::id (-> i :store/state ::running-id))))

(defmethod transition :toaster/show [i]
  (let [toast (store/msg-payload i)
        toast-with-id (assoc-running-id toast i)]
    (-> i
        (update-in [:store/state ::running-id] inc)
        (update-in [:store/state ::toasts] conj toast-with-id)
        (update-in [:store/effs] conj [::hide-toast! toast-with-id]))))

(defmethod store/eff! ::hide-toast! [i]
  (let [toast (store/eff-payload i)]
    (go
      (<! (timeout (-> toast :toast/duration (or 0))))
      (store/put! i [::toast-duration-elapsed toast]))))

(defmethod transition ::toast-duration-elapsed [i]
  (let [toast (store/msg-payload i)
        toasts (-> i :store/state ::toasts)
        toasts-new (remove #{toast} toasts)]
    (-> i
        (assoc-in [:store/state ::toasts] toasts-new))))

(defn view [i]
  [:div {:style {:position :absolute
                 :top 0
                 :left 0
                 :pointer-events :none
                 :z-index 1000
                 :width "100%"
                 :display :flex
                 :flex-direction :column
                 :align-items :center
                 :justify-content :center}}
   (for [toast (-> i :store/state ::toasts)]
     ^{:key toast}
     [:article
      (str (:toast/message toast))])])

