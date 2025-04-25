(ns app.frontend.toaster
  (:require
   [app.frontend.mod :as mod]
   [app.frontend.toast :as toast]
   [clojure.core.async :as a]
   [core.program :as p]
   [core.ui.icon :as icon]
   [core.ui.icon-button :as icon-button]))

(defn- init []
  {::toast-queue []
   ::toast nil
   ::toast-state nil})

(defn exiting? [state]
  (-> state ::toast-state (= :toast-state/exit)))

(defn to-next-toast [state]
  (let [toast-queue (-> state ::toast-queue)]
    (last toast-queue)))

(defn- remove-toast [toasts toast]
  (filterv #(not= (:toast/id %) (:toast/id toast)) toasts))

(defn- logic [i]
  (p/reg-reducer i ::init (fn [state _] (merge state (init))))
  (a/go (p/put! i [::init]))

  (p/reg-reducer
   i ::set-toast
   (fn [state [_ toast]] (assoc state ::toast toast)))

  (p/reg-reducer
   i ::set-toast-state
   (fn [state [_ toast-state]] (assoc state ::toast-state toast-state)))

  (p/reg-reducer
   i ::enqueue-toast
   (fn [state [_ toast]]
     (-> state (update ::toast-queue conj toast))))

  (p/reg-reducer
   i ::dequeue-toast
   (fn [state [_ toast]]
     (-> state (update ::toast-queue remove-toast toast))))

  (p/take-every!
   i :toaster/show
   (fn [[_ toast]]
     (p/put! i [::enqueue-toast toast])))

  (a/go-loop []
    (let [toast (to-next-toast (p/state! i))]
      (when (nil? toast)
        (a/<! (p/take! i ::enqueue-toast))
        (recur))

      (p/put! i [::set-toast toast])

      (p/put! i [::set-toast-state :toast-state/enter])

      (a/alt! (a/timeout (:toast/duration toast)) ::timeout
              (p/take! i ::clicked-dismiss) ::clicked-dismiss
              (p/take! i ::enqueue-toast) ::enqueue-toast)

      (p/put! i [::set-toast-state :toast-state/exit])

      (a/<! (a/timeout 300))

      (p/put! i [::dequeue-toast toast])

      (recur))))




(defn- view-dismiss-button [i]
  [icon-button/view
   {:icon-button/on-click #(p/put! i [::clicked-dismiss])
    :icon-button/view-icon icon/x-mark}])

(defn- toast-classes [i toast]
  (str
   "slide-in-from-top "
   (when (exiting? i) "slide-out-to-top ")
   (case (toast/variant toast)
     :toast-variant/info "bg-neutral-800"
     :toast-variant/error "bg-red-500"
     "bg-neutral-800")))

(defn- view-toast-content [i toast]
  [:div.flex.items-center.justify-start.w-full
   [:p.flex-1 (toast/message toast)]
   [view-dismiss-button i]])

(defn- view-toast [i toast]
  [:div.p-3.shadow-2xl.rounded.text-lg.pointer-events-auto.flex.items-center.justify-start.font-bold
   {:id (toast/id toast)
    :class (toast-classes i toast)}
   [view-toast-content i toast]])

(defn- view [i]
  (when-let [toast (-> i ::toast)]
    [:div.absolute.top-0.left-0.w-full.p-4.pointer-events-none
     [view-toast i toast]]))

(mod/reg
 {:mod/name :mod/toaster
  :mod/view-fn view
  :mod/logic-fn logic})