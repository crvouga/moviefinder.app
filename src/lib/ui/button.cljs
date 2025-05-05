(ns lib.ui.button
  (:require
   [lib.ui.cn :refer [cn]]
   [lib.ui.icon :as icon]))

(def button-type->html-type
  {:button/submit "submit"
   :button/reset "reset"
   :button/button "button"})

(defmulti classes-color :button/color)
(defmethod classes-color :default []
  "bg-blue-500 text-white")
(defmethod classes-color :button/color-neutral [] "bg-neutral-700 text-white")

(defn- classes [i]
  (cn
   "py-3 px-4 rounded flex items-center justify-center font-bold text-lg relative cursor-pointer touch-manipulation "

   (classes-color i)

   (if (-> i :button/full?) "w-full " "w-fit ")

   (when (not (-> i :button/loading?))
     "hover:opacity-75 focus:opacity-75 active:opacity-75 ")

   (when (-> i :button/loading?)
     "cursor-wait opacity-75 ")))



(defn- view-spinner [i]
  (when (-> i :button/loading?)
    [:div.absolute.inset-0.flex.items-center.justify-center
     (icon/spinner {:class "size-10 animate-spin"})]))

(defn- view-label [i]
  [:div {:class (when (-> i :button/loading?) "opacity-0")} (-> i :button/label)])

(defn view [i]
  [:button
   {:type (-> i :button/type button-type->html-type (or "button"))
    :disabled (-> i :button/loading?)
    :on-click (-> i :button/on-click)
    :aria-busy (-> i :button/loading?)
    :class (classes i)}
   (view-spinner i)
   (view-label i)])