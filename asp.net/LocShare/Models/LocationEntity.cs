namespace LocShare.Models
{
    using Newtonsoft.Json;
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;

    [Table("location")]
    public partial class LocationEntity
    {
        [Key]
        [Column("id")]
        public int Id { get; set; }

        [Required]
        [StringLength(50)]
        [Column("username")]
        public string Username { get; set; }

        [Column("latitude")]
        public double? Latitude { get; set; }

        [Column("longitude")]
        public double? Longitude { get; set; }

        [Column("altitude")]
        [JsonProperty("altitude")]
        public double? Altitude { get; set; }

        [Column("accuracy")]
        public double? Accuracy { get; set; }

        [Column("speed")]
        public double? Speed { get; set; }

        [Column("time")]
        public DateTime Time { get; set; }
    }
}