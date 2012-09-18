$(function () {
    "use strict";

    var detect = $('#detect');
    var header = $('#header');
    var content = $('#content');
    var input = $('#input');
    var status = $('#status');
    var myName = false;
    var author = null;
    var logged = false;
    var loginurl = "";
    var pathname = document.location.pathname;
    var lastdot = pathname.lastIndexOf("/");
    if (lastdot > 1) {
        loginurl = pathname.substr(1, lastdot);
    }
    
    var client_opts = {"url":"http://localhost:8080/chat","sockjs_opts":{"devel":true,"debug":true}};
    
    function log(a) {
            if ('console' in window && 'log' in window.console) {
                console.log(a);
            }
            $('#logs').append($("<code>").text(a));
            $('#logs').append($("<br>"));
            $('#logs').scrollTop($('#logs').scrollTop()+10000);
      }

    var sjs = null;
    var protocol;
    var options;
    
    $('#connect').click(function() {
        $('#connect').attr('disabled', true);
        $('#disconnect').each(function(_,e){e.disabled='';});
        var protocol;
        if (protocol === 'not-websocket') {
            protocol = ['xdr-streaming',
                      'xhr-streaming',
                      'iframe-eventsource',
                      'iframe-htmlfile',
                      'xdr-polling',
                      'xhr-polling',
                      'iframe-xhr-polling',
                      'jsonp-polling'];
        }
        log('[connecting] ' + protocol);
        options = jQuery.extend({}, client_opts.sockjs_opts)
        options.protocols_whitelist = typeof protocol === 'string' ?
                                                        [protocol] : protocol;
        sjs = new SockJS(client_opts.url + '/broadcast', null, options);
        sjs.onopen = onopen
        sjs.onclose = onclose;
        sjs.onmessage = xonmessage;
    });
    $('#disconnect').click(function() {
        $('#disconnect').attr('disabled', true);
        log('[disconnecting]');
        sjs.close();
    });

    var onopen = function() {
        log('connected ' + sjs.protocol);
    };
    var onclose = function(e) {
        log('disconnected ' + e);
        $('#connect').each(function(_,e){e.disabled='';});
        $('#disconnect').attr('disabled', true);
    };
    var myself = (''+Math.random()).substr(2);
    var xonmessage = function(e) {
        var msg = JSON.parse(e.data);
        if (msg.id === myself) {
            var td = (new Date()).getTime() - msg.t;
            $('#latency').text('' + td + ' ms');
        }
        var id = 'cursor_'+msg.id;
        if ($('#'+id).length === 0) {
            $("body").append('<div id="' + id + '" class="cursor"></div>');
        }
        $('#'+id).offset({top:msg.y-15, left:msg.x-15});
    };
    var x, y;
    var last_x, last_y, tref;
    $(document).mousemove(function(e) {
         x = e.pageX; y = e.pageY;
         if(!tref) poll();
    });
    var poll = function() {
         tref = null;
         if (last_x === x && last_y === y)
             return;
         var msg = {x:x, y:y, t: (new Date()).getTime(), id:myself};
         last_x = x; last_y = y;
         var raw_msg = JSON.stringify(msg);
         if (sjs && sjs.readyState === SockJS.OPEN) {
             sjs.send(raw_msg);
         }
         tref = setTimeout(poll, 200);
    };
    $('#connect').each(function(_,e){e.disabled='';});
    $('#disconnect').attr('disabled', true);
    
});

