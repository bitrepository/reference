
  function FileSizeUtils() {
    
    var unitSize = 1024;
    var byteSize = 1;
    var kiloSize = byteSize * unitSize;
    var megaSize = kiloSize * unitSize;
    var gigaSize = megaSize * unitSize;
    var teraSize = gigaSize * unitSize;
    var petaSize = teraSize * unitSize;
    var exaSize = petaSize * unitSize;

    var bytePostfix = "B";
    var kiloPostfix = "KB";
    var megaPostfix = "MB";
    var gigaPostfix = "GB";
    var teraPostfix = "TB";
    var petaPostfix = "PB";
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
      } else if (petaPostfix == unit) {
        return petaSize;
      } else if (teraPostfix == unit) {
        return teraSize;
      } else if (petaPostfix == unit) {
        return petaSize;
      } else if (gigaPostfix == unit) {
        return gigaSize;
      } else if (megaPostfix == unit) {
        return megaSize;
      } else if (kiloPostfix == unit) {
        return kiloSize;
      } else if (bytePostfix == unit) {
        return byteSize;
      } else {
        return byteSize;
      }
    }
  }
