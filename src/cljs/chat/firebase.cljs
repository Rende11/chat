(ns chat.firebase)

(def config {
             :apiKey "AIzaSyCEhmVGg3qnpqSnPwAFpCHdRqwsR5abkhU"
             :authDomain "clj-chat.firebaseapp.com"
             :databaseURL "https://clj-chat.firebaseio.com"
             :projectId "clj-chat"
             :storageBucket "clj-chat.appspot.com"
             :messagingSenderId "293369282958"
             })

(defn init []
  (js/firebase.initializeApp (clj->js config)))

(def firebase (init))

(defn create-github-provider []
  (let [p (->> firebase
               .-firebase_
               .-auth
               .-GithubAuthProvider)]
    (p.)))

(defn sign-in-with-popup [provider]
  (-> (.auth firebase)
      (.signInWithPopup provider)))

(defn sign-in-with-github []
  (-> (create-github-provider)
      sign-in-with-popup
      ))

(defn connect-db []
  (.database firebase))

(defn on-messages [room-ref cb]
  (.on room-ref "value" #(-> (.val %)
                             (js->clj :keywordize-keys true)
                             vals
                             cb)))

(defn subscribe-room [room cb]
  (let [db (connect-db)]
    (-> (.ref db (str "rooms/" room))
        (on-messages cb))))
