(ns lib.ui.bottom-buttons
  (:require
   [lib.ui.bar :as bar]
   [lib.ui.cn :refer [cn]]))

(defn to-btn-class [i]
  (cn
   "flex flex-col items-center justify-center flex-1 h-full text-sm gap-0.5 cursor-pointer"
   (if (-> i :bottom-buttons/selected?)
     "text-blue-500"
     "text-white")))

(defn- view-btn [i]
  [:button
   {:on-pointer-down (-> i :bottom-button/on-click)
    :class (to-btn-class i)}
   (when-let [view-icon (-> i :bottom-button/view-icon)]
     [view-icon {:class "size-7 flex items-center justify-center"}])
   [:p (-> i :bottom-button/label)]])

(defn view [i]
  [:div.flex.w-full.items-center {:class bar/h-class}
   (for [button (-> i :bottom-buttons/buttons)]
     ^{:key (-> button :bottom-button/label)}
     [view-btn button])])
