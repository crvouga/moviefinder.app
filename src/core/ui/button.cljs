(ns core.ui.button
  (:require [core.ui.icon :as icon]))

(def button-type->html-type
  {:button-type/submit "submit"
   :button-type/reset "reset"
   :button-type/button "button"})

(defn- button-classes [i]
  (str
   "bg-blue-500 text-white py-3 px-4 rounded flex items-center justify-center font-bold text-lg relative cursor-pointer "

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
    :class (button-classes i)}
   (view-spinner i)
   (view-label i)])