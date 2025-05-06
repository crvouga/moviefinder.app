(ns lib.ui.drawer
  (:require
   [lib.ui.cn :refer [cn]]))

(defn- concatv [v1 v2]
  (vec (concat v1 v2)))


(defn view [i & children]
  [:div
   {:id (i :drawer/id)
    :aria-hidden (-> i :drawer/open? not)
    :role "dialog"
    :aria-label (i :drawer/aria-label)
    :class (cn
            "absolute inset-0 z-50 flex bg-black/80 transition-opacity duration-200"
            (if (= (i :drawer/position) :drawer/top)
              "flex-col-reverse "
              "flex-col")
            (if (i :drawer/open?)
              "pointer-events-auto opacity-100"
              "pointer-events-none opacity-0"))}
   [:div
    {:class "w-full flex-1"
     :aria-label "Close"
     :on-click (i :drawer/on-close)}]
   (concatv
    [:div
     {:class (cn
              "bg-black h-fit w-full border border-neutral-800 transition-transform duration-200"
              (if (= (i :drawer/position) :drawer/top)
                (cn "rounded-b-2xl"
                    (if (i :drawer/open?) "translate-y-0" "-translate-y-full"))
                (cn "rounded-t-2xl" (if (i :drawer/open?) "translate-y-0" "translate-y-full"))))}]
    children)])
