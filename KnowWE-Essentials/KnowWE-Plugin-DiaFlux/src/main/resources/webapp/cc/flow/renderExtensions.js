Array.prototype.flatten = function() {
    return this.inject([], function(array, value) {
      if (DiaFluxUtils.isArray(value))
        return array.concat(value.flatten());
      array.push(value);
      return array;
    });
  }

Array.prototype.inject = function(memo, iterator, context) {
    this.each(function(value, index) {
      memo = iterator.call(context, memo, value, index);
    });
    return memo;
  }


String.prototype.startsWith = function (pattern) {
    return this.indexOf(pattern) === 0;
  }

String.prototype.endsWith = function(pattern) {
    var d = this.length - pattern.length;
    return d >= 0 && this.lastIndexOf(pattern) === d;
  }


