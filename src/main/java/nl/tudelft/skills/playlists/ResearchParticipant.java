package nl.tudelft.skills.playlists;

import lombok.*;
import nl.tudelft.skills.model.labracore.SCPerson;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResearchParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @OneToOne
    @JoinColumn(unique = true)
    private SCPerson person;

    @NotNull
    @Builder.Default
    private LocalDateTime optIn = LocalDateTime.now();

    private LocalDateTime optOut;

}
