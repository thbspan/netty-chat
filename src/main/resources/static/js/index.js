var Chat = (function () {
    function Chat() {
        this.wsPath = "ws://" + window.location.host + "/ws";
        this.nickname = null;
        this.socket = null;
    }

    // Chat.prototype.constructor = Chat
    Chat.prototype.login = function () {
        var nickname = $("#nickname").val();
        if (!nickname) {
            alert("请先输入昵称");
            return;
        }
        this.init(nickname);
    };

    Chat.prototype.init = function (nickname) {
        var self = this;
        self.nickname = nickname;

        // 隐藏 con-login
        $(".con-login").hide();

        // 初始化 socket
        if (!window.WebSocket) {
            window.WebSocket = window.MozWebSocket;
        }
        if (window.WebSocket) {
            self.socket = new WebSocket(self.wsPath);
            self.socket.onopen = function () {
                // 连接成功
                // 显示聊天界面
                $(".con-chat").show();
                $(".con-chat-nav .chat-nickname").text(self.nickname);
                self.sendLoginMessage();
            };
            self.socket.onmessage = function (event) {
                // 收到消息进行处理
                self.process(event.data);
            };
            self.socket.onclose = function () {
                // 连接关闭后回调
                console.log("websocket connection is closed")
            };
        } else {
            alert("Your browser does not support Web Socket.");
        }
    };

    Chat.prototype.sendLoginMessage = function () {
        this.sendMessage('LOGIN');
    };

    Chat.prototype.sendMessage = function (msgType, content) {
        if (this.socket.readyState === window.WebSocket.OPEN) {
            var msg = msgType + "|" + Date.now() + "|" + this.nickname;
            if (content) {
                msg += "|" + content;
            }
            this.socket.send(msg);
        }
    };

    Chat.prototype.sendUserInput = function () {
        var $content = $(".con-chat-main .chat-input");
        var content = $content.text().trim();

        if (content) {
            this.sendMessage('CHAT', content)
            $content.text("");
        }
    };
    Chat.prototype.process = function (msg) {
        var parts = msg.split("|");
        var msgType = parts[0];
        if ("SYSTEM" === msgType) {
            this.processSystem(parts);
        } else if ("CHAT" === msgType) {
            this.processChat(parts);
        }
    };

    Chat.prototype.processSystem = function (parts) {
        // 更新当前在线人数信息
        $(".con-chat-nav .chat-online-count").text(parts[2]);
        // 在聊天框中显示系统消息
        if (parts[3]) {
            $(".chat-msg").append("<h3>" + parts[3] + "</h3>");
        }
    };

    Chat.prototype.processChat = function (parts) {
        var sender = parts[2];
        // 在聊天框中显示消息
        $(".chat-msg").append("<p><b>" + (sender === this.nickname ? "我" : sender) + ":</b><span>" + Chat.dateFormat("YYYY-mm-dd HH:MM:SS", new Date(+parts[1])) + "</span>" + parts[3] + "</p>");
    };

    Chat.dateFormat = function (fmt, date) {
        var ret;
        var opt = {
            "Y+": date.getFullYear().toString(),        // 年
            "m+": (date.getMonth() + 1).toString(),     // 月
            "d+": date.getDate().toString(),            // 日
            "H+": date.getHours().toString(),           // 时
            "M+": date.getMinutes().toString(),         // 分
            "S+": date.getSeconds().toString()          // 秒
            // 有其他格式化字符需求可以继续添加，必须转化成字符串
        };
        for (var k in opt) {
            ret = new RegExp("(" + k + ")").exec(fmt);
            if (ret) {
                fmt = fmt.replace(ret[1], (ret[1].length === 1) ? (opt[k]) : (opt[k].padStart(ret[1].length, "0")))
            }
        }
        return fmt;
    };
    return Chat;
}());
window.CHAT = new Chat();
