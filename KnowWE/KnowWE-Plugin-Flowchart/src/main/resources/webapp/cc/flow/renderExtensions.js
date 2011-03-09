Array.prototype.flatten = function() {
    return this.inject([], function(array, value) {
      if (DiaFluxUtils.isArray(value))
        return array.concat(value.flatten());
      array.push(value);
      return array;
    });
  }



