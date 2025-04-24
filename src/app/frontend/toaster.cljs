(ns app.frontend.toaster
  (:require [clojure.core.async :refer [<! go-loop timeout alts!]]
            [core.program :as p]
            [core.ui.icon-button :as icon-button]
            [core.ui.icon :as icon]))

(go-loop []
  (let [msg (<! (p/take! :toaster/show))
        toast (second msg)
        toast-id (-> (p/read!) ::running-id)
        toast-with-id (assoc toast :toast/id toast-id)
        duration (or (:toast/duration toast) 0)

        _ (p/put! [::add-toast toast-with-id])

        _ (p/put! [::increment-running-id])

        _ (<! (alts!
               (p/take! ::clicked-close-toast-button)
               (timeout duration)))

        _ (p/put! [::mark-toast-exiting toast-id])

        _ (<! (timeout 500))

        _ (p/put! [::remove-toast toast-id])]
    (recur)))

(p/reg-reducer
 ::add-toast
 (fn [state msg]
   (update state ::toasts conj (second msg))))

(p/reg-reducer
 ::increment-running-id
 (fn [state _]
   (update state ::running-id inc)))

(p/reg-reducer
 ::mark-toast-exiting
 (fn [state msg]
   (update state ::exiting-ids conj (second msg))))

(p/reg-reducer
 ::remove-toast
 (fn [state msg]
   (update state ::toasts disj (second msg))))

;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defn- class-toast-variant [variant]
  (case variant
    :toast-variant/info " bg-neutral-800"
    :toast-variant/error " bg-red-600"
    " bg-neutral-800"))

(defn- class-toast-animation [exiting?]
  (if exiting? " slide-out-to-top" ""))

(defn- view-close-button [toast-id]
  [icon-button/view
   {:icon-button/on-click #(p/put! [::clicked-close-toast-button toast-id])
    :icon-button/view-icon icon/x-mark}])

(defn- view-toast-message [message]
  [:p.flex-1 (str message)])

(defn- toast-id [toast]
  (:toast/id toast))

(defn- toast-variant [toast]
  (or (:toast/variant toast) :toast-variant/info))

(defn- toast-message [toast]
  (:toast/message toast))

(defn- is-exiting? [toast-id exiting-ids]
  (contains? exiting-ids toast-id))

(defn- toast-element-id [toast-id]
  (str "toast-" toast-id))

(defn- toast-classes [exiting? variant]
  (str (class-toast-animation exiting?)
       (class-toast-variant variant)))

(defn- view-toast-content [toast toast-id]
  [:div.flex.items-center.justify-start.w-full
   [view-toast-message (toast-message toast)]
   [view-close-button toast-id]])

(defn- view-single-toast [toast exiting-ids]
  (let [id (toast-id toast)
        exiting? (is-exiting? id exiting-ids)
        variant (toast-variant toast)]
    ^{:key id}
    [:div.p-3.shadow-2xl.rounded.text-lg.pointer-events-auto.flex.items-center.justify-start.font-bold.slide-in-from-top
     {:id (toast-element-id id)
      :class (toast-classes exiting? variant)}
     [view-toast-content toast id]]))

(defn- view-toast-container [children]
  [:div.absolute.top-0.left-0.w-full.p-4.pointer-events-none
   children])

(defn view [input]
  (let [toasts (-> input ::toasts)
        exiting-ids (-> input ::exiting-ids)]
    [view-toast-container
     (for [toast toasts]
       ^{:key (:toast/id toast)}
       [view-single-toast toast exiting-ids])]))
