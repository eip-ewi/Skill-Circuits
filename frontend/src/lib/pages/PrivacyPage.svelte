<script lang="ts">

import PageLayout from "./PageLayout.svelte";
import Link from "../components/util/Link.svelte";
</script>

<PageLayout>
    <div class="content">
        <div>
            <h1>Privacy statement</h1>
        </div>
        <p>
            <strong class="fw-500">
                This privacy statement is a first version. Some aspects in this privacy
                statement might be changed in the future to be in line with the law and best
                practices
            </strong>
        </p>

        <p>
            The Skill Circuits Project is a project from the TU Delft. In this privacy
            statement, it will be explained what personal data the Skill Circuits collects when
            using the website. In this document the term Labrador will also be mentioned.
            Labrador is a project of the TU Delft, which will include the Skill Circuits.
        </p>
        <h2 id="topics">Topics</h2>
        <ul>
            <li>What data do we collect?</li>
            <li>How do we collect your data?</li>
            <li>How will we use your data?</li>
            <li>How will we store your data?</li>
            <li>Other matters</li>
        </ul>
        <h2 id="what-data-do-we-collect">What data do we collect</h2>
        <p>
            To support your education at the Delft University of Technology , the university has
            a legal ground to store your data. To be more specific, it is a contractual
            necessity: the data processing is necessary for the performance of the contract
            between the student and Delft University of Technology . Because of this, the Skill
            Circuits collects the following data:
        </p>
        <ul>
            <li>Name</li>
            <li>Email</li>
            <li>Student number</li>
            <li>NetID</li>
            <li>affiliation (if the user is a student or teacher)</li>
            <li>which department/faculty the user is part of</li>
        </ul>
        <p>
            Furthermore, there exist multiple subgroups in the system. We will describe for each
            subgroup the extra data we store.
        </p>
        <h3 id="student">Student</h3>
        <p>We collect the following information about students:</p>
        <ul>
            <li>Courses they are enrolled in</li>
        </ul>
        <p>
            We also collect data about the tasks students complete, so that progress can be
            synchronised across devices and we can refer students back to the last task they
            completed:
        </p>
        <ul>
            <li>Which tasks a student has completed and when</li>
            <li>Which tasks a student has selected to do on their path</li>
        </ul>
        <h3 id="teacher">Teacher</h3>
        <p>For a teacher, we collect the following data:</p>
        <ul>
            <li>All courses the teacher teaches</li>
        </ul>
        <h2 id="how-do-we-collect-your-data">How do we collect your data</h2>
        <p>All data we collect is collected when using the Skill Circuits website.</p>

        <h2 id="how-will-we-use-your-data">How will we use your data</h2>
        <p>
            At this moment, we only use the data given to support the functionality the Skill
            Circuits promise, giving students an overview of what tasks they still need to
            complete and where they left of.
        </p>
        <p>
            Data is being used on a need to know basis. Only administrators have access to the
            database.
        </p>
        <p>
            Data from the Skill Circuits is is also used for research purposes. This research is
            done to explore the possibilities of increasing educational quality and investigate
            what type of materials are most effective to teach students about certain concepts.
        </p>
        <p>For this research the following additional data will be stored:</p>
        <ul>
            <li>What links were clicked when by whom</li>
        </ul>
        <p>
            This data will only be accessible to people doing the research and will be
            responsibly handled in such a way that no unauthorised person or persons can get
            access to this data.
        </p>
        <h2 id="how-do-we-store-your-data">How do we store your data</h2>
        <p>
            To comply with GDPR regulations, all data is stored on site at the Delft University
            of Technology. The data stored on our servers will be deleted after 5 years.
        </p>
        <p>
            Access to data is strictly regulated with only select admins having access to the
            database. All other entities need to use the website itself, where, as mentioned
            previously, information is provided on a need to know basis.
        </p>
        <h1 id="other-matters">Other matters</h1>
        <p>For matters like:</p>
        <ul>
            <li>What are your data protection rights</li>
            <li>Which cookies we set and why</li>
        </ul>
        <p>
            Please refer to the
            <Link href="https://www.tudelft.nl/privacy-statement/">Privacy Statement of the TU Delft</Link>.
        </p>
        <p>
            If you have any questions about the privacy policy or want to make use of your
            rights, please contact
            <Link href="mailto:privacy-tud@tudelft.nl">
                the following email address
            </Link>.
        </p>
    </div>
</PageLayout>

<style>
    h1 {
        font-size: var(--font-size-700);
        font-weight: 700;
        margin-bottom: .2em;
    }
    h2 {
        font-size: var(--font-size-500);
        font-weight: 500;
        margin-bottom: .2em;
    }
    h3 {
        font-size: var(--font-size-400);
        font-weight: 500;
        margin-bottom: .2em;
    }

    ul {
        margin-left: 2em;
    }

    :where(p, ul) + :where(p, ul) {
        margin-top: .5em;
    }
    :where(p, ul) + :where(h1, h2, h3) {
        margin-top: 1em;
    }

    .content {
        margin-bottom: 6rem;
    }
</style>