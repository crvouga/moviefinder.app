(ns linkpage.frontend.ui.form)

(defn view [props & children]
  [:form
   (merge props
          {:on-submit (-> props :form/on-submit)})
   (for [c children]
     ^{:key c} c)])
  