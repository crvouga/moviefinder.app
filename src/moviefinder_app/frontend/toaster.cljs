(ns moviefinder-app.frontend.toaster
  (:require [moviefinder-app.frontend.store :as store]))

(store/reg!
 :store/initialized
 (fn [i]
   (-> i
       (update-in [:store/state] assoc
                  ::toasts []
                  ::exiting-ids #{}
                  ::running-id 0)))

 :toaster/show
 (fn [i]
   (let [toast (store/msg-payload i)
         toast-with-id (-> toast (assoc :toast/id (-> i :store/state ::running-id)))]
     (-> i
         (update-in [:store/state ::running-id] inc)
         (assoc-in [:store/state ::toasts] #{toast-with-id})
         (update-in [:store/effs] conj [:runtime/sleep {:sleep/duration (-> toast :toast/duration (or 0))
                                                        :sleep/msgs #{[::toast-duration-elapsed toast-with-id]}}]))))

 ::toast-duration-elapsed
 (fn [i]
   (let [toast (store/msg-payload i)
         exiting-ids-new (-> i :store/state ::exiting-ids (into #{}) (conj (:toast/id toast)))]
     (-> i
         (update :store/state assoc ::exiting-ids exiting-ids-new)))))

(defn view [i]
  [:div.absolute.top-0.left-0.w-full.p-4.pointer-events-none
   (for [toast (-> i :store/state ::toasts)
         :let [exiting? (contains? (-> i :store/state ::exiting-ids) (:toast/id toast))
               toast-id (:toast/id toast)]]
     ^{:key toast-id}
     [:div.p-3.bg-neutral-800.shadow-2xl.rounded.text-lg.pointer-events-auto.flex.items-center.justify-start.font-bold.slide-in-from-top
      {:id (str "toast-" toast-id)
       :class (str (when exiting? " slide-out-to-top"))}
      (str (:toast/message toast))])])

