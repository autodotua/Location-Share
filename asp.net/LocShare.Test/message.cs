namespace LocShare.Test
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Data.Entity.Spatial;

    [Table("message")]
    public partial class message
    {
        [Key]
        [Column(Order = 0)]
        public int id { get; set; }

        [Key]
        [Column(Order = 1)]
        [StringLength(50)]
        public string user_name_from { get; set; }

        [Key]
        [Column(Order = 2)]
        [StringLength(50)]
        public string user_name_to { get; set; }

        [Column("message")]
        public string message1 { get; set; }

        [Key]
        [Column(Order = 3)]
        public byte type { get; set; }

        [Key]
        [Column(Order = 4)]
        public DateTime time { get; set; }
    }
}
