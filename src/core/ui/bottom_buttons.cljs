(ns core.ui.bottom-buttons)


(defn view [i]
  [:div.flex.w-full.items-center.h-20.shrink-0
   (for [button (-> i :bottom-buttons/buttons)
         :let [on-click (-> button  :bottom-button/on-click)
               label (-> button :bottom-button/label)
               view-icon (-> button :bottom-button/view-icon)
               selected? (-> button :bottom-button/selected? (or false))]]
     ^{:key label}
     [:button {:on-click on-click
               :class (str
                       "flex flex-col items-center justify-center flex-1 h-full text-base text-sm gap-0.5 cursor-pointer "
                       (if selected? "text-blue-500 " "text-white "))}
      (when view-icon
        [view-icon {:class "size-7 flex items-center justify-center"}])
      [:p label]])])