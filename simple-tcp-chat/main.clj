; primirive TPC chat

(require '[clojure.java.io :as io])
(import '[java.net ServerSocket SocketException])

(def users (atom {}))

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn sock-send [sock msg]
  (let [out (io/writer sock)]
    (.write out msg)
    (.flush out)))

(defn sock-read-line [sock]
  (let [inp (io/reader sock)]
    (.readLine inp)))

(defn broadcast [sender-id msg]
  (doseq [pair @users]
    (let [
          usr (last pair)
          usr-id (:id usr)
          online (:online usr)
          sock (:socket usr)]
      (when (and online (not= sender-id usr-id))
        (sock-send sock msg)))))

(defn accept-client [usr-id usr]
  (let [sock (:socket usr)]
    (future
      ; request user name
      (sock-send sock "Enter your name: ")
      (reset! users
              (-> @users
                  (assoc-in [usr-id :name] (sock-read-line sock))
                  (assoc-in [usr-id :online] true)))

      ; users main loop
      (try
        (while (not (.isClosed sock))
          (let [sender-name (:name (get @users usr-id))
                msg (sock-read-line sock)]
            (broadcast usr-id (str "[" sender-name "]: " msg "\n"))))
        (catch SocketException e (println "socket closed")))
      (reset! users (dissoc @users usr-id))
      (println (str "client " usr-id " has been disconnected"))
      (.close sock)
      nil)))

(defn disconnect-all-users [usrs]
  (doseq [usr usrs]
    (-> usr
        last
        (get :socket)
        .close)))

(defn tcp-chat-server [port]
  (let [
        running (atom true)]
    (future
      (with-open [s (ServerSocket. port)]
        (while @running
          (println "Waiting for client...")
          (let [sock (.accept s)
                usr-id (rand-str 10)
                usr {
                     :id usr-id
                     :name nil
                     :online false
                     :socket sock
                     }]
            (println (str "Client has been connected with id=" usr-id))
            (reset! users (conj @users {usr-id usr}))
            (accept-client usr-id usr)))))
    {
     :close (fn []
              (reset! running false)
              (disconnect-all-users @users))
     }))

(tcp-chat-server 6669)
