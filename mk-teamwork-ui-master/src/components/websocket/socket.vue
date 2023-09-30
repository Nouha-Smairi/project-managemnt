<template>
    <div>
    </div>
</template>

<script>
    import Vue from 'vue'
    import {getStore, setStore} from '@/assets/js/storage'
    import {bindClientId} from "../../api/common";
    import config from "../../config/config";


    export default {
        data() {
            return {
                websocket: null,
            }
        },
        methods: {
            threadPoxi() {  // the actual method called
                //parameter
                const agentData = {uid: this.$store.state.userInfo.id};
                //If ws is on
                if (this.websocket.readyState === this.websocket.OPEN) {
                    this.websocketSend(agentData)
                }
                // If it is in the open state, wait for 300 milliseconds
                else if (this.websocket.readyState === this.websocket.CONNECTING) {
                    let that = this;//Save the current object this
                    setTimeout(function () {
                        that.websocketSend(agentData)
                    }, 300);
                }
                // If not enabled, wait 500 milliseconds
                else {
                    this.initWebSocket();
                    let that = this;//Save the current object this
                    setTimeout(function () {
                        that.websocketSend(agentData)
                    }, 500);
                }
            },
            initWebSocket() { //Initialize weosocket
                //ws address
                const WS_URI = config.WS_URI;
                this.websocket = new WebSocket(WS_URI);
                this.websocket.onmessage = this.websocketOnMessage;
                this.websocket.onclose = this.websocketClose;
                this.websocket.onopen = this.websocketOnOpen
                this.websocket.onerror = this.websocketOnError
                Vue.prototype.$websocket = this.websocket;

            },
            websocketOnOpen () {
                console.log('WebSocketconnection succeeded')
            },
            websocketOnError (e) {
                console.log('WebSocketconnection error')
            },
            websocketOnMessage(e) { //data reception
                const data = JSON.parse(e.data);
                if (data.action != 'ping') {
                    console.log(data);
                    this.$store.commit('catchSocketAction', data);
                }
                if (data.action === 'connect') {
                    if (data.data.client_id) {
                        if (!this.$store.state.boundClient) {
                            setStore('client_id', data.data.client_id);
                            const userInfo = getStore('userInfo', true);
                            const uid = userInfo ? userInfo.code : '';
                            if (uid) {
                                bindClientId(data.data.client_id, uid);
                                this.$store.dispatch('setBoundClient', true);
                            }
                        }
                    }
                }
            },
            websocketSend(agentData) {//data transmission
                console.log("Send a message:");
                console.log(agentData);
                this.websocket.send(agentData);
            },
            websocketClose() {  //closure
                debugger;
                console.log("connection closed ");
            }
        },
        created() {
            this.initWebSocket()
        }
    }
</script>
