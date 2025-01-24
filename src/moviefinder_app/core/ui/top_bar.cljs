(ns moviefinder-app.core.ui.top-bar
  (:require
   [moviefinder-app.core.ui.icon-button :as icon-button]
   [moviefinder-app.core.ui.icon :as icon]))


(defn view [i]
  (let [title (-> i :top-bar/title)
        on-back (-> i :top-bar/on-back)]
    [:nav.flex.items-center.justify-center.w-full.h-20.px-4
     (when on-back
       (icon-button/view {:icon-button/on-click on-back
                          :icon-button/view-icon icon/arrow-left  }))
     [:p.font-bold.flex-1.text-lg.text-center title]
     [:div.size-8]]))