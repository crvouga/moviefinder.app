(ns lib.ui.icon-button
  (:require
   [lib.ui.children :as children]))

(defn- button? [i]
  (or (-> i :icon-button/on-click)
      (-> i :icon-button/on-pointer-down)))

(defn- element [i] (if (button? i) :button :div))

(defn base [i attrs & children]
  (children/with
   [(element i)
    (merge {:class "size-8 rounded-full text-white cursor-pointer aspect-square"} attrs)] children))

(defn whitespace []
  [base {:disabled true  :aria-hidden true}])

(defn view [i]
  [base i
   {:on-click (-> i :icon-button/on-click)
    :on-pointer-down (-> i :icon-button/on-pointer-down)}
   (when (-> i :icon-button/view-icon)
     [(-> i :icon-button/view-icon) {:class "w-full h-full"}])])