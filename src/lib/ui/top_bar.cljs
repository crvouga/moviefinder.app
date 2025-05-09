(ns lib.ui.top-bar
  (:require
   [lib.ui.bar :as bar]
   [lib.ui.icon :as icon]
   [lib.ui.icon-button :as icon-button]))


(defn view [i]
  [:div.w-full.shrink-0.flex.items-center.px-4
   {:class bar/h-class}
   (when-not (-> i :top-bar/on-back)
     [icon-button/whitespace])
   (when (-> i :top-bar/on-back)
     (icon-button/view {:icon-button/on-pointer-down (-> i :top-bar/on-back)
                        :icon-button/view-icon icon/arrow-left}))
   [:p.font-bold.flex-1.text-lg.text-center (-> i :top-bar/title)]
   [icon-button/whitespace]])