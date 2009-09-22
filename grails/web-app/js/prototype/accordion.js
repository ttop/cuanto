//by Valerio Proietti (http://mad4milk.net). MIT-style license.
//accordion.js - depends on prototype.js or prototype.lite.js + moo.fx.js
//version 2.0

//modified by Suk-Hyun Cho (Apr. 05, 2009)
//1.    Options now extend "trigger", which defines the type of event that triggers the accordion to expand/collapse.
//      Available trigger types and their corresponding javascript event type:
//          'click' -> onclick
//          'hover' -> onmouseover
//      the default is 'click'.
//2.    Now using the javascript module pattern for that clean, fuzzy feeling.
//3.    Options now extend "triggerThreshold", which defines the duration of time to be consumed before firing triggers.
//      Consequently, hovering over many menus will not "shiver" all the items, as long as the threshold is big enough.
//      As for click actions, clicking multiple items will cause the only last action to be triggered.
//      The unit is in milliseconds, and the default is 0 (immediate trigger).

Fx.Accordion = Class.create();
Fx.Accordion.prototype = Object.extend(new Fx.Base(), function() {

    var pub = {};
    var that;
    var timer;

    pub.initialize = function(togglers, elements, options) {
        that = this;
        this.now = {};
        this.elements = $A(elements);
        this.togglers = $A(togglers);
        this.setOptions(options);
        extendOptions(options);
        this.previousTrigger = 'nan';
        this.togglers.each(function(tog, i) {
            switch (this.options.trigger) {
                case 'click':
                    bindToClick(tog, i); break;
                case 'hover':
                    bindToHover(tog, i); break;
            }

        }.bind(this));
        this.h = {};
        this.w = {};
        this.o = {};
        this.elements.each(function(el, i) {
            this.now[i + 1] = {};
            el.style.height = '0';
            el.style.overflow = 'hidden';
        }.bind(this));
        switch (this.options.start) {
            case 'first-open': this.elements[0].style.height = this.elements[0].scrollHeight + 'px'; break;
            case 'open-first': showThisHideOpen(0); break;
        }
    };

    pub.setNow = function() {
        for (var i in this.from) {
            var iFrom = this.from[i];
            var iTo = this.to[i];
            var iNow = this.now[i] = {};
            for (var p in iFrom) iNow[p] = this.compute(iFrom[p], iTo[p]);
        }
    };

    pub.increase = function() {
        for (var i in this.now) {
            var iNow = this.now[i];
            for (var p in iNow) this.setStyle(this.elements[parseInt(i) - 1], p, iNow[p]);
        }
    };

    /* private helper functions */

    function extendOptions(options) {
        Object.extend(that.options, Object.extend({
            start: 'open-first',
            fixedHeight: false,
            fixedWidth: false,
            alwaysHide: false,
            wait: false,
            onActive: function() {
            },
            onBackground: function() {
            },
            height: true,
            opacity: true,
            width: false,
            trigger: 'click',
            triggerThreshold: 0
        }, options || {}));
    }

    function custom(objObjs) {
        if (that.timer && that.options.wait) return null;
        var from = {};
        var to = {};
        for (var i in objObjs) {
            var iProps = objObjs[i];
            var iFrom = from[i] = {};
            var iTo = to[i] = {};
            for (var prop in iProps) {
                iFrom[prop] = iProps[prop][0];
                iTo[prop] = iProps[prop][1];
            }
        }
        return that._start(from, to);
    }

    function hideThis(i) {
        if (that.options.height) that.h = {'height': [that.elements[i].offsetHeight, 0]};
        if (that.options.width) that.w = {'width': [that.elements[i].offsetWidth, 0]};
        if (that.options.opacity) that.o = {'opacity': [that.now[i + 1]['opacity'] || 1, 0]};
    }

    function showThis(i) {
        if (that.options.height) that.h = {'height': [that.elements[i].offsetHeight, that.options.fixedHeight || that.elements[i].scrollHeight]};
        if (that.options.width) that.w = {'width': [that.elements[i].offsetWidth, that.options.fixedWidth || that.elements[i].scrollWidth]};
        if (that.options.opacity) that.o = {'opacity': [that.now[i + 1]['opacity'] || 0, 1]};
    }

    function showThisHideOpen(iToShow) {
        if (iToShow != that.previousTrigger || that.options.alwaysHide) {
            that.previousTrigger = iToShow;
            var objObjs = {};
            var err = false;
            var madeInactive = false;
            that.elements.each(function(el, i) {
                that.now[i] = that.now[i] || {};
                if (i != iToShow) {
                    hideThis(i);
                } else if (that.options.alwaysHide) {
                    if (el.offsetHeight == el.scrollHeight) {
                        hideThis(i);
                        madeInactive = true;
                    } else if (el.offsetHeight == 0) {
                        showThis(i);
                    } else {
                        err = true;
                    }
                } else if (that.options.wait && that.timer) {
                    that.previousTrigger = 'nan';
                    err = true;
                } else {
                    showThis(i);
                }
                objObjs[i + 1] = Object.extend(that.h, Object.extend(that.o, that.w));
            }.bind(that));
            if (err) return null;
            if (!madeInactive) that.options.onActive.call(that, that.togglers[iToShow], iToShow);
            that.togglers.each(function(tog, i) {
                if (i != iToShow || madeInactive) that.options.onBackground.call(that, tog, i);
            }.bind(that));
            return custom(objObjs);
        } else return null;
    }

    /* trigger binding */

    function bindToClick(tog, i) {
        if (tog.onclick) tog.prevTrigger = tog.onclick;
        else tog.prevTrigger = function() {};
        $(tog).onclick = function() {

            // stop the pending tirgger if there is a click on another menu
            if (timer) {
                clearTimeout(timer);
                timer = null;
            }
            
            timer = setTimeout(function() {
                tog.prevTrigger();
                showThisHideOpen(i);
                timer = null;
            }, that.options.triggerThreshold < 0 ? 0 : that.options.triggerThreshold);
        }.bind(that);
    }

    function bindToHover(tog, i) {
        if (tog.onmouseover) tog.prevTrigger = tog.onmouseover;
        else tog.prevTrigger = function() {};

        // stop the pending trigger if the mouse is out of the menu
        $(tog).onmouseout = function() {
            clearTimeout(timer);
            timer = null;
        }

        $(tog).onmouseover = function() {

            // deactivate all togglers
            that.togglers.each(function(toDeactivate, i) {
                toDeactivate.className = 'inactiveToggler';
            });

            // activate the current toggler
            tog.className = 'activeToggler round2';
            timer = setTimeout(function() {
                tog.prevTrigger();
                showThisHideOpen(i);
                timer = null;
            }, that.options.triggerThreshold < 0 ? 0 : that.options.triggerThreshold);
        }.bind(that);
    }

    return pub;
}());
