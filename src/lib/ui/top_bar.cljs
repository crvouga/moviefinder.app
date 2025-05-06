(ns lib.ui.top-bar
  (:require
   [lib.ui.children :as children]
   [lib.ui.icon :as icon]
   [lib.ui.icon-button :as icon-button]))


(defn root [props & children]
  (children/with [:div.w-full.h-20.shrink-0.flex.items-center.px-4 props] children))

(defn view [i]
  [root
   (when-not (-> i :top-bar/on-back)
     [icon-button/whitespace])
   (when (-> i :top-bar/on-back)
     (icon-button/view {:icon-button/on-pointer-down (-> i :top-bar/on-back)
                        :icon-button/view-icon icon/arrow-left}))
   [:p.font-bold.flex-1.text-lg.text-center (-> i :top-bar/title)]
   [icon-button/whitespace]])