(ns lib.ui.confirmation
  (:require
   [lib.ui.drawer :as drawer]
   [lib.ui.button :as button]))

(defn view-buttons [i]
  [:div.w-full.flex.gap-3.pt-4
   [button/view {:button/full? true
                 :button/label (-> i :confirmation/cancel-text)
                 :button/on-click (-> i :confirmation/on-cancel)
                 :button/loading? (-> i :confirmation/cancel-loading?)
                 :button/color :button/color-neutral}]
   [button/view {:button/full? true
                 :button/label (-> i :confirmation/confirm-text)
                 :button/on-click (-> i :confirmation/on-confirm)
                 :button/type (when-not (-> i :confirmation/on-confirm) :button/type-submit)
                 :button/loading? (-> i :confirmation/confirm-loading?)}]])


(defn view-drawer [i]
  [drawer/view
   {:drawer/open? (-> i :confirmation/open?)
    :drawer/on-close (-> i :confirmation/on-close)
    :drawer/id (-> i :confirmation/id)
    :drawer/aria-label (-> i :confirmation/aria-label)
    :drawer/position (-> i :confirmation/position)}
   [:div.p-6.flex.flex-col.gap-4
    [:h2.text-2xl.font-bold (-> i :confirmation/title)]
    [:p.text-sm.text-neutral-400 (-> i :confirmation/description)]
    (view-buttons i)]])