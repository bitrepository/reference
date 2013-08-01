
  function FileSizeUtils() {
    
    var unitSize = 1024;
    var byteSize = 1;
    var kiloSize = byteSize * unitSize;
    var megaSize = kiloSize * unitSize;
    var gigaSize = megaSize * unitSize;
    var teraSize = gigaSize * unitSize;
    var petaSize = teraSize * unitSize;
    var exaSize = petaSize * unitSize;

    var bytePostFix = "B";
    var kiloPostFix = "KB";
    var megaPostFix = "MB";
    var gigaPostFix = "GB";
    var teraPostFix = "TB";
    var petaPostFix = "PB";
    var exaPostfix = "EB";


    this.toHumanUnit = function(size) {
      if (size == null) {
        return bytePostfix;
      }
      if (size >= exaSize) {
        return exaPostfix;
      } else if (size >= petaSize) {
        return petaPostfix;
      } else if (size >= teraSize) {
        return teraPostfix;
      } else if (size >= gigaSize) {
        return gigaPostfix;
      } else if (size >= megaSize) {
        return megaPostfix;
      } else if (size >= kiloSize) {
        return kiloPostfix;
      } else {
        return bytePostfix;
      }
    }

    this.getByteSize = function(unit) {
      if (exaPostfix.equals(unit)) {
        return exaSize;
      } else if (petaPostfix.equals(unit)) {
        return petaSize;
      } else if (teraPostfix.equals(unit)) {
        return teraSize;
      } else if (petaPostfix.equals(unit)) {
        return petaSize;
      } else if (gigaPostfix.equals(unit)) {
        return gigaSize;
      } else if (megaPostfix.equals(unit)) {
        return megaSize;
      } else if (kiloPostfix.equals(unit)) {
        return kiloSize;
      } else if (bytePostfix.equals(unit)) {
        return byteSize;
      } else {
        return byteSize;
      }
    }
  }
