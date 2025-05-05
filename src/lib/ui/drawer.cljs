(ns lib.ui.drawer
  (:require
   [lib.ui.cn :refer [cn]]))

(defn- concatv [v1 v2]
  (vec (concat v1 v2)))


(defn view
  [{:keys [drawer/open?
           drawer/on-close
           drawer/id
           drawer/aria-label
           drawer/position]}
   & children]
  [:div
   {:id id
    :aria-hidden (not open?)
    :role "dialog"
    :aria-label aria-label
    :class (cn
            "absolute inset-0 z-50 flex bg-black/80 transition-opacity duration-200"
            (if (= position :drawer/top)
              "flex-col-reverse "
              "flex-col")
            (if open?
              "pointer-events-auto opacity-100"
              "pointer-events-none opacity-0"))}
   [:div
    {:class "w-full flex-1"
     :aria-label "Close"
     :on-click on-close}]
   (concatv
    [:div
     {:class (cn
              "bg-neutral-950 h-fit w-full border border-neutral-800 transition-transform duration-200"
              (if (= position :drawer/top)
                (cn "rounded-b-2xl" (if open? "translate-y-0" "-translate-y-full"))
                (cn "rounded-t-2xl" (if open? "translate-y-0" "translate-y-full"))))}]
    children)])
