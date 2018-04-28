AUI.add("aui-ace-editor-mode-python",function(a){define("ace/mode/python",["require","exports","module","pilot/oop","ace/mode/text","ace/tokenizer","ace/mode/python_highlight_rules","ace/mode/matching_brace_outdent","ace/range"],function(t,s,r){var q=t("pilot/oop"),p=t("ace/mode/text").Mode,o=t("ace/tokenizer").Tokenizer,n=t("ace/mode/python_highlight_rules").PythonHighlightRules,m=t("ace/mode/matching_brace_outdent").MatchingBraceOutdent,l=t("ace/range").Range,k=function(){this.$tokenizer=new o((new n).getRules()),this.$outdent=new m;};q.inherits(k,p),function(){this.toggleCommentLines=function(D,C,B,A){var z=!0,y=[],x=/^(\s*)#/;for(var w=B;w<=A;w++){if(!x.test(C.getLine(w))){z=!1;break;}}if(z){var v=new l(0,0,0,0);for(var w=B;w<=A;w++){var u=C.getLine(w),i=u.match(x);v.start.row=w,v.end.row=w,v.end.column=i[0].length,C.replace(v,i[1]);}}else{C.indentRows(B,A,"#");}},this.getNextLineIndent=function(j,i,z){var y=this.$getIndent(i),x=this.$tokenizer.getLineTokens(i,j),w=x.tokens,v=x.state;if(w.length&&w[w.length-1].type=="comment"){return y;}if(j=="start"){var u=i.match(/^.*[\{\(\[\:]\s*$/);u&&(y+=z);}return y;},this.checkOutdent=function(e,d,f){return this.$outdent.checkOutdent(d,f);},this.autoOutdent=function(e,d,f){this.$outdent.autoOutdent(d,f);};}.call(k.prototype),s.Mode=k;}),define("ace/mode/python_highlight_rules",["require","exports","module","pilot/oop","pilot/lang","ace/mode/text_highlight_rules"],function(i,h,n){var m=i("pilot/oop"),l=i("pilot/lang"),k=i("ace/mode/text_highlight_rules").TextHighlightRules,j=function(){var F=l.arrayToMap("and|as|assert|break|class|continue|def|del|elif|else|except|exec|finally|for|from|global|if|import|in|is|lambda|not|or|pass|print|raise|return|try|while|with|yield".split("|")),E=l.arrayToMap("True|False|None|NotImplemented|Ellipsis|__debug__".split("|")),D=l.arrayToMap("abs|divmod|input|open|staticmethod|all|enumerate|int|ord|str|any|eval|isinstance|pow|sum|basestring|execfile|issubclass|print|super|binfile|iter|property|tuple|bool|filter|len|range|type|bytearray|float|list|raw_input|unichr|callable|format|locals|reduce|unicode|chr|frozenset|long|reload|vars|classmethod|getattr|map|repr|xrange|cmp|globals|max|reversed|zip|compile|hasattr|memoryview|round|__import__|complex|hash|min|set|apply|delattr|help|next|setattr|buffer|dict|hex|object|slice|coerce|dir|id|oct|sorted|intern".split("|")),C=l.arrayToMap("".split("|")),B="(?:r|u|ur|R|U|UR|Ur|uR)?",A="(?:(?:[1-9]\\d*)|(?:0))",z="(?:0[oO]?[0-7]+)",y="(?:0[xX][\\dA-Fa-f]+)",x="(?:0[bB][01]+)",w="(?:"+A+"|"+z+"|"+y+"|"+x+")",v="(?:[eE][+-]?\\d+)",u="(?:\\.\\d+)",t="(?:\\d+)",s="(?:(?:"+t+"?"+u+")|(?:"+t+"\\.))",r="(?:(?:"+s+"|"+t+")"+v+")",e="(?:"+r+"|"+s+")";this.$rules={start:[{token:"comment",regex:"#.*$"},{token:"string",regex:B+'"{3}(?:[^\\\\]|\\\\.)*?"{3}'},{token:"string",merge:!0,regex:B+'"{3}.*$',next:"qqstring"},{token:"string",regex:B+'"(?:[^\\\\]|\\\\.)*?"'},{token:"string",regex:B+"'{3}(?:[^\\\\]|\\\\.)*?'{3}"},{token:"string",merge:!0,regex:B+"'{3}.*$",next:"qstring"},{token:"string",regex:B+"'(?:[^\\\\]|\\\\.)*?'"},{token:"constant.numeric",regex:"(?:"+e+"|\\d+)[jJ]\\b"},{token:"constant.numeric",regex:e},{token:"constant.numeric",regex:w+"[lL]\\b"},{token:"constant.numeric",regex:w+"\\b"},{token:function(b){return F.hasOwnProperty(b)?"keyword":E.hasOwnProperty(b)?"constant.language":C.hasOwnProperty(b)?"invalid.illegal":D.hasOwnProperty(b)?"support.function":b=="debugger"?"invalid.deprecated":"identifier";},regex:"[a-zA-Z_$][a-zA-Z0-9_$]*\\b"},{token:"keyword.operator",regex:"\\+|\\-|\\*|\\*\\*|\\/|\\/\\/|%|<<|>>|&|\\||\\^|~|<|>|<=|=>|==|!=|<>|="},{token:"lparen",regex:"[\\[\\(\\{]"},{token:"rparen",regex:"[\\]\\)\\}]"},{token:"text",regex:"\\s+"}],qqstring:[{token:"string",regex:'(?:[^\\\\]|\\\\.)*?"{3}',next:"start"},{token:"string",merge:!0,regex:".+"}],qstring:[{token:"string",regex:"(?:[^\\\\]|\\\\.)*?'{3}",next:"start"},{token:"string",merge:!0,regex:".+"}]};};m.inherits(j,k),h.PythonHighlightRules=j;}),define("ace/mode/matching_brace_outdent",["require","exports","module","ace/range"],function(g,f,j){var i=g("ace/range").Range,h=function(){};(function(){this.checkOutdent=function(d,c){return/^\s+$/.test(d)?/^\s*\}/.test(c):!1;},this.autoOutdent=function(k,d){var p=k.getLine(d),o=p.match(/^(\s*\})/);if(!o){return 0;}var n=o[1].length,m=k.findMatchingBracket({row:d,column:n});if(!m||m.row==d){return 0;}var l=this.$getIndent(k.getLine(m.row));k.replace(new i(d,0,d,n-1),l);},this.$getIndent=function(d){var c=d.match(/^(\s+)/);return c?c[1]:"";};}).call(h.prototype),f.MatchingBraceOutdent=h;});},"1.0.1",{requires:["aui-ace-editor-base"],skinnable:false});