(ns app.frontend.toaster
  (:require
   [app.frontend.mod :as mod]
   [cljs.pprint :as pprint]
   [clojure.core.async :as a]
   [core.program :as p]
   [core.ui.icon :as icon]
   [core.ui.icon-button :as icon-button]))


(defn- ensure-toasts [state]
  (update state ::toasts #(or % {})))

(defn- ensure-removing-ids [state]
  (update state ::removing-ids #(or % #{})))

(defn- ensure-running-id [state]
  (update state ::running-toast-id #(or % 0)))

(defn- logic [i]
  (p/reg-reducer i ::inc-running-toast-id (fn [state _] (-> state ensure-running-id (update ::running-toast-id inc))))

  (p/reg-reducer
   i ::added
   (fn [state [_ toast]]
     (-> state ensure-toasts (update ::toasts assoc (:toast/id toast) toast))))

  (p/reg-reducer
   i ::finished-removing
   (fn [state [_ toast-id]]
     (-> state
         ensure-toasts
         (update ::toasts dissoc toast-id)
         (update ::removing-ids disj toast-id))))

  (p/reg-reducer
   i ::started-removing
   (fn [state [_ toast-id]]
     (-> state
         ensure-removing-ids
         (update ::removing-ids conj toast-id))))

  ;; Create a channel for close button clicks
  (let [close-chan (a/chan)]
    ;; Listen for close button clicks
    (a/go-loop []
      (when-let [msg (a/<! (p/take! i ::clicked-close-toast-button))]
        (a/put! close-chan msg)
        (recur)))

    ;; Main toast loop
    (a/go-loop []
      (let [[_ toast] (a/<! (p/take! i :toaster/show))
            state (p/state! i)
            toast-id (-> state ensure-running-id ::running-toast-id)
            toast (assoc toast :toast/id toast-id)]

        (p/put! i [::inc-running-toast-id])
        (p/put! i [::added toast])

        ;; Wait for either timeout or close button
        (let [[v _] (a/alts! [(a/timeout (:toast/duration toast))
                              close-chan])]
          (when (not= v :timeout)
            (p/put! i [::started-removing (:toast/id toast)])
            #_(a/<! (dom/animation-end-chan! (toast-element-id toast-id)))
            (a/<! (a/timeout 500))
            (p/put! i [::finished-removing (:toast/id toast)])))

        (recur)))))


(defn- class-toast-variant [variant]
  (case variant
    :toast-variant/info " bg-neutral-800"
    :toast-variant/error " bg-red-500"
    " bg-neutral-800"))

(defn- class-toast-animation [exiting?]
  (if exiting? " slide-out-to-top" ""))

(defn- view-close-button [i toast-id]
  [icon-button/view
   {:icon-button/on-click #(p/put! i [::clicked-close-toast-button toast-id])
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

(defn- view-toast-content [i toast toast-id]
  [:div.flex.items-center.justify-start.w-full
   [view-toast-message (toast-message toast)]
   [view-close-button i toast-id]])

(defn- view-single-toast [i toast exiting-ids]
  (let [id (toast-id toast)
        exiting? (is-exiting? id exiting-ids)
        variant (toast-variant toast)]
    (pprint/pprint {"toast" toast})
    [:div.p-3.shadow-2xl.rounded.text-lg.pointer-events-auto.flex.items-center.justify-start.font-bold.slide-in-from-top
     {:id (toast-element-id id)
      :class (toast-classes exiting? variant)}
     [view-toast-content i toast id]]))

(defn- view-toast-container [children]
  (vec (concat [:div.absolute.top-0.left-0.w-full.p-4.pointer-events-none] children)))

(defn- view [i]
  (let [toasts (-> i ::toasts)
        exiting-ids (-> i ::removing-ids)]
    [view-toast-container
     (for [toast (vals toasts)]
       ^{:key (toast-id toast)}
       [view-single-toast i toast exiting-ids])]))

(mod/reg
 {:mod/name :mod/toaster
  :mod/view-fn view
  :mod/logic-fn logic})