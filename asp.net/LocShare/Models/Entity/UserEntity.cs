namespace LocShare.Models.Entity
{
    using Newtonsoft.Json;
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;

    [Table("login_user")]
    public partial class UserEntity
    {
        [Key]
        [StringLength(50)]
        [Column("name")]
        [JsonProperty("name")]
        public string Name { get; set; }

        [StringLength(255)]
        [Column("display_name")]
        [JsonProperty("displayName")]
        public string DisplayName { get; set; }

        [Required]
        [StringLength(32)]
        [Column("password")]
        [JsonProperty("password")]
        public string Password { get; set; }

        //[Required]
        [StringLength(255)]
        [Column("group_name")]
        [JsonProperty("groupName")]
        public string GroupName { get; set; } = "";

        [Column("last_update_time")]
        [JsonProperty("lastUpdateTime")]
        public DateTime? LastUpdateTime { get; set; }

        [NotMapped]
        [JsonProperty("token")]
        public string Token { get; set; }

        [NotMapped]
        [JsonProperty("lastLocation")]
        public LocationEntity LastLocation { get; set; }

        [Column("last_location_id")]
        public int? LastLocationId { get; set; }

        public UserEntity ApplyLastLocation(LocationEntity location)
        {
            LastLocation = location;
            return this;
        }

        public override string ToString()
        {
            return Name + "£¨" + DisplayName + "£© - " + GroupName;
        }
    }
}