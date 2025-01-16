(ns linkpage.frontend.toaster
  (:require [linkpage.frontend.store :as store]
            [clojure.core.async :refer [go <! timeout]]))

(store/reg!
 :toaster/show
 (fn [i]
   (let [toast (store/msg-payload i)
         toast-with-id (-> toast (assoc ::id (-> i :store/state ::running-id)))]
     (-> i
         (update-in [:store/state ::running-id] inc)
         (update-in [:store/state ::toasts] conj toast-with-id)
         (update-in [:store/effs] conj [::hide-toast! toast-with-id]))))

 :store/initialized
 (fn [i]
   (-> i
       (update-in [:store/state] assoc ::toasts [])
       (update-in [:store/state] assoc ::running-id 0)))

 ::toast-duration-elapsed
 (fn [i]
   (let [toast (store/msg-payload i)
         toasts (-> i :store/state ::toasts)
         toasts-new (remove #{toast} toasts)]
     (-> i
         (assoc-in [:store/state ::toasts] toasts-new))))

 ::toast-duration-elapsed
 (fn [i]
   (let [toast (store/msg-payload i)
         toasts (-> i :store/state ::toasts)
         toasts-new (remove #{toast} toasts)]
     (-> i
         (assoc-in [:store/state ::toasts] toasts-new))))

 ::toast-duration-elapsed
 (fn [i]
   (let [toast (store/msg-payload i)
         toasts (-> i :store/state ::toasts)
         toasts-new (remove #{toast} toasts)]
     (-> i
         (assoc-in [:store/state ::toasts] toasts-new)))))

(store/reg-eff!
 ::hide-toast!
 (fn [i]
   (let [toast (store/eff-payload i)]
     (go
       (<! (timeout (-> toast :toast/duration (or 0))))
       (store/put! i [::toast-duration-elapsed toast])))))

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

