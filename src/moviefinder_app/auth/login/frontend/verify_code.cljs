(ns moviefinder-app.auth.login.frontend.verify-code
  (:require
   [moviefinder-app.frontend.screen :as screen]
   [moviefinder-app.frontend.store :as store]
   [moviefinder-app.core.result :as result]
   [moviefinder-app.core.ui.button :as button]
   [moviefinder-app.auth.login.frontend.shared :refer [view-layout]]
   [moviefinder-app.core.ui.text-field :as text-field]))

(store/reg!
 :store/initialized
 (fn [i]
   (-> i
       (assoc-in [:store/state ::request] [:result/not-asked])))


 ::inputted-code
 (fn [i]
   (-> i
       (assoc-in [:store/state ::code] (store/msg-payload i))))


 ::submitted-verify-code-form
 (fn [i]
   (let [rpc-req [:login-rpc/verify-code
                  {:user/phone-number (-> i screen/screen-payload :user/phone-number)
                   :verify-sms/code (-> i :store/state ::code)}]]
     (-> i
         (update-in [:store/state] assoc ::request [:result/loading])
         (update-in [:store/effs] conj [:rpc/send! {:rpc/req rpc-req
                                                    :rpc/res #(vector ::backend-verified-code %)}]))))

 ::backend-verified-code
 (fn [i]
   (let [payload (store/msg-payload i)
         msgs (when (result/ok? payload)
                [[:login/authenticated payload]])]
     (-> i
         (update-in [:store/state] assoc ::request payload)
         (update-in [:store/msgs] concat msgs)))))


(defn loading? [i]
  (-> i :store/state ::request result/loading?))

(screen/reg!
 :route/login-verify-code
 (fn [i]
   [view-layout "Verify Code"
    [:form.flex.flex-col.w-full.gap-6
     {:on-submit #(do (.preventDefault %) (store/put! i [::submitted-verify-code-form]))}
     [:p.text-lg "Enter the code we sent to " [:span.font-bold (-> i screen/screen-payload :user/phone-number)]]
     [text-field/view
      {:text-field/label "Code"
       :text-field/value (-> i :store/state ::code)
       :text-field/required? true
       :text-field/disabled? (loading? i)
       :text-field/on-change #(store/put! i [::inputted-code %])}]
     [:div.w-full]
     [button/view
      {:button/type :button-type/submit
       :button/loading? (loading? i)
       :button/full? true
       :button/label "Verify Code"}]]]))