namespace LocShare.Test
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Data.Entity.Spatial;

    [Table("location")]
    public partial class location
    {
        public int id { get; set; }

        [Required]
        [StringLength(50)]
        public string username { get; set; }

        public double? latitude { get; set; }

        public double? longitude { get; set; }

        public double? altitude { get; set; }

        public double? accuracy { get; set; }

        public double? speed { get; set; }

        public DateTime time { get; set; }
    }
}
