(ns lib.ui.icon-button
  (:require
   [lib.ui.children :as children]))

(defn base [& children]
  (children/with [:button.size-8.rounded-full.text-white.cursor-pointer.aspect-square] children))

(defn whitespace []
  [base {:disabled true  :aria-hidden true}])

(defn view [i]
  [base
   {:on-click (-> i :icon-button/on-click)
    :on-pointer-down (-> i :icon-button/on-pointer-down)}
   (when (-> i :icon-button/view-icon)
     [(-> i :icon-button/view-icon) {:class "w-full h-full"}])])