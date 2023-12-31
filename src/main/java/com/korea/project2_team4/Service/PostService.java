package com.korea.project2_team4.Service;

import com.korea.project2_team4.Model.Entity.*;
import com.korea.project2_team4.Repository.MemberRepository;
import com.korea.project2_team4.Repository.PostRepository;
import com.korea.project2_team4.Repository.ProfileRepository;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder
@Service
public class PostService {
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final MemberRepository memberRepository;
    private final ImageService imageService;

    public void save(Post post) {
        postRepository.save(post);
    }

    public Page<Post> postList(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return postRepository.findAll(pageable);
    }

    public Page<Post> getPostsByTagName(int page, String searchTagName) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return postRepository.findByTagName(searchTagName, pageable);
    }

    public Page<Post> getPostsOrderByLikeCount(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return postRepository.findAllOrderByLikeMembersSizeDesc(pageable);
    }

    public Page<Post> getPostsOrderByCommentCount(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return postRepository.findAllOrderByCommentsSizeDesc(pageable);
    }

    public Page<Post> getMyPosts(int page, Profile author) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return postRepository.findByAuthor(author, pageable);
    }

    public Page<Post> getMyLikedPosts(int page, Member member) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return postRepository.findByLikeMembers(member, pageable);

    }

    //테스트 데이터
    @PostConstruct
    public void init() {
        saveTestPost();
    }

    //테스트 데이터
    @Transactional
    public void saveTestPost() {
        if (postRepository.findAll().isEmpty()) {

            Member admin = memberRepository.findByUserName("admin").orElse(null);

            Profile authorProfile = profileRepository.findByProfileName("관리자")
                    .orElseGet(() -> {
                        Profile newProfile = new Profile();
                        newProfile.setProfileName("관리자");
                        return newProfile;
                    });

            // Profile 저장 (만약 새로 생성한 경우에만 저장)
            if (!profileRepository.existsById(authorProfile.getId())) {
                profileRepository.save(authorProfile);
            }

            for (int i = 1; i <= 10; i++) {
                Post post = new Post();
                post.setTitle(String.format("테스트 데이터 제목 입니다:[%03d].", i));
                post.setContent("테스트 데이터 내용 입니다.");
                post.setCreateDate(LocalDateTime.now());

                post.setAuthor(admin.getProfile());

                postRepository.save(post);
            }
        }
    }


    // 게시물 검색기능
    public List<Post> searchPosts(String kw) {
        return postRepository.findAllByKw(kw);
    }

    public List<Post> searchPostTitle(String kw) {
        return postRepository.findByPostTitle(kw);
    }

    public List<Post> searchPostContent(String kw) {
        return postRepository.findByPostContent(kw);
    }

    public List<Post> searchProfileName(String kw) {
        return postRepository.findByProfileName(kw);
    }

    public List<Post> searchCommentContent(String kw) {
        return postRepository.findByCommentContent(kw);
    }

    // post를 optional타입으로 가져오기
    public Post getPost(Long id) {
        Optional<Post> postOptional = postRepository.findById(id);
        return postOptional.orElse(null);

    }

    public Post getPostIncrementView(Long id) {
        Optional<Post> postOptional = postRepository.findById(id);
        if (postOptional.isPresent()) {
            Post postView = postOptional.get();
            postView.setView(postView.getView() + 1);
            return postRepository.save(postView);
        }
        return postOptional.orElse(null);
    }

    //좋아요 기능
    public void Like(Post post, Member member) {
        post.getLikeMembers().add(member);
        this.postRepository.save(post);
    }

    public void unLike(Post post, Member member) {
        post.getLikeMembers().remove(member);
        this.postRepository.save(post);
    }

    public boolean isLiked(Post post, Member member) {
        if (post == null) {
            return false;
        }
        return post.getLikeMembers().contains(member);
    }

    public void deleteById(Long id) {
        Optional<Post> optionalPost = postRepository.findById(id);

        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();

            List<Image> postImages = post.getPostImages();

            if (postImages != null) {
                for (Image image : postImages) {
                    String filepath = image.getFilePath();

                    if (filepath != null && !filepath.isEmpty()) {
                        imageService.deleteExistingFile(filepath);
                    }
                }
            }

            this.postRepository.deleteById(id);
        }
    }

//   ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ 선영 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    //작성자게시글불러오기
    public List<Post> getPostsbyAuthor(Profile profile) {
        List<Post> targetPosts = this.postRepository.findAllByauthor(profile.getProfileName());
        return targetPosts;
    }

    //작성자 게시글 -> 페이징처리
    public Page<Post> myPostListPage(int page, Profile profile) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return postRepository.findAllByauthorPage(profile.getProfileName(), pageable);
    }
//
//    public Page<Post> getPostsByCategory(Page<Post> tagNamedPosts, int page, String category) {
//        List<Post> newlist = new ArrayList<>();
//        for (Post post : tagNamedPosts.getContent()) {
//            if (post.getCategory().equals(category)) {
//                newlist.add(post);
//            }
//        } // 태그+카테고리 화 된 리스트 +sort..
////a밑에페이징처리학.;ㅏㅓㅣㅏㅓㅣㅏ;
//        List<Sort.Order> sorts = new ArrayList<>();
//        sorts.add(Sort.Order.desc("createDate"));
//        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
//        return postRepository.findAllBycategory(category, pageable);
//    }
//
//    public Page<Post> getPostsBysort(Page<Post> taggedcategoriedPosts, int page, String sort) {
//
//        Pageable pageable = PageRequest.of(page, 10);
//        if (sort.equals("likeCount")) {
//            return postRepository.findAllByTagNameAndCategoryOrderByLikeMembersSizeDesc(tagname, category, pageable);
//        } else {
////            return postRepository.findAllByTagNameAndCategoryOrderByCommentsSizeDesc(tagname, category, pageable);
//        }
//
////페이징처리 ㅜ
//        List<Sort.Order> sorts = new ArrayList<>();
//        sorts.add(Sort.Order.desc("createDate"));
////        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
////        return postRepository.findAllBycategory(category, pageable);
//    }


    public Page<Post> getPostsBycategoryAndsortAndtag(int page, String category, String sort, String tagname) {
        Pageable pageable = PageRequest.of(page, 10);
        if (sort.equals("likeCount")) {
            return postRepository.findAllByTagNameAndCategoryOrderByLikeMembersSizeDesc(tagname, category, pageable);
        } else {
            return postRepository.findAllByTagNameAndCategoryOrderByCommentsSizeDesc(tagname, category, pageable);
        }
    }


    //findAll 메서드 하나 만들고
    // 리스트? 페이지? 로 받아와서 category별 정렬 메서드, sort별, tagname별 세개 따로 메서드 만들고싶은데,
    //페이징처리가 껴있어서 어떻게해야되는지 모르겠음.
    // 예:
    // public Page<Post> getPostsBycategory (Page<Post>, String category) { return postRepository.~~~ }
    // public Page<Post> getPostsBysort (Page<Post>, String sort) { return postRepository.~~~ }
    // public Page<Post> getPostsBytagName (Page<Post>, String tagname) { return postRepository.~~~ }


//    public Page<Post> getPostsBytagAndcategoryAndsort(int page, String tagName, String category, String sort) {
//        Pageable pageable = PageRequest.of(page, 10);
//
//        if ( (tagName != null) || (category != null) ) {
//            if (sort.equals("likeCount")) {
//                return this.postRepository.findAllByTagNameAndCategoryOrderByLikeMembersSizeDesc(tagName, category, pageable);
//            } else {
//                return this.postRepository.findAllByTagNameAndCategoryOrderByCommentsSizeDesc(tagName, category, pageable);
//            }
//
//            if (category == null ) { //태그만 잇고 sort두가지
//                if (sort.equals("likeCount")) {
//                   return this.postRepository.findAllByTagNameOrderByLikeMembersSizeDesc(tagName, pageable);
//                } else {
//                    return this.postRepository.findAllByTagNameOrderByCommentsSizeDesc(tagName, pageable);
//                }
//            }
//            if (tagName == null ) { //카테고리만 있고 sort두가지
//                if (sort.equals("likeCount")) {
//                    return this.postRepository.findAllByCategoryAndOrderByLikeMembersSizeDesc(category, pageable);
//                } else {
//                    return this.postRepository.findAllByCategoryAndOrderByCommentsSizeDesc(category, pageable);
//                }
//            }
//        } else { //태그 카테고리 둘다 null
//            if (sort.equals("likeCount")) {
//                return this.postRepository.findAllOrderByLikeMembersSizeDesc(pageable);
//            } else {
//                return this.postRepository.findAllOrderByCommentsSizeDesc(pageable);
//            }
//        }
//
//    }

    public Page<Post> getPostsBytagAndcategoryAndsort(int page, String tagName, String category, String sort) {
        Pageable pageable = PageRequest.of(page, 10);

        if (tagName != null || category != null) {
            if (category == null) { // 태그만 있고 sort 두 가지
                if (sort.equals("likeCount")) {
                    return this.postRepository.findAllByTagNameOrderByLikeMembersSizeDesc(tagName, pageable);
                } else if (sort.equals("commentCount")) {
                    return this.postRepository.findAllByTagNameOrderByCommentsSizeDesc(tagName, pageable);
                } else {
                    return this.postRepository.findAllByTagName(tagName,pageable);
                }
            } else if (tagName == null) { // 카테고리만 있고 sort 두 가지
                if (sort.equals("likeCount")) {
                    return this.postRepository.findAllByCategoryAndOrderByLikeMembersSizeDesc(category, pageable);
                } else if (sort.equals("commentCount")) {
                    return this.postRepository.findAllByCategoryAndOrderByCommentsSizeDesc(category, pageable);
                } else {
                    return this.postRepository.findAllByCategory(category, pageable);
                }
            } else { // 태그, 카테고리 둘 다 있고 sort 두 가지
                if (sort.equals("likeCount")) {
                    return this.postRepository.findAllByTagNameAndCategoryOrderByLikeMembersSizeDesc(tagName, category, pageable);
                } else if (sort.equals("commentCount")) {
                    return this.postRepository.findAllByTagNameAndCategoryOrderByCommentsSizeDesc(tagName, category, pageable);
                } else {
                    return this.postRepository.findAllByTagNameAndCategory(tagName,category,pageable);
                }
            }
        } else { // 태그, 카테고리 둘 다 null

            if (sort.equals("likeCount")) {
                return this.postRepository.findAllOrderByLikeMembersSizeDesc(pageable);
            } else if (sort.equals("commentCount")) {
                return this.postRepository.findAllOrderByCommentsSizeDesc(pageable);
            } else {
                return this.postRepository.findAll(pageable);
            }


        }
    }


//   ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ 선영 ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

        public Page<Post> pagingByTitle (String kw,int page){
            List<Sort.Order> sorts = new ArrayList<>();
            sorts.add(Sort.Order.desc("id"));
            Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));
            return postRepository.findByPostTitleFromPaging(kw, pageable);
        }

        public Page<Post> pagingByContent (String kw,int page){
            List<Sort.Order> sorts = new ArrayList<>();
            sorts.add(Sort.Order.desc("id"));
            Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));
            return postRepository.findByPostContentFromPaging(kw, pageable);
        }

        public Page<Post> pagingByProfileName (String kw,int page){
            List<Sort.Order> sorts = new ArrayList<>();
            sorts.add(Sort.Order.desc("id"));
            Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));
            return postRepository.findByProfileNameFromPaging(kw, pageable);
        }

        public Page<Post> pagingByComment (String kw,int page){
            List<Sort.Order> sorts = new ArrayList<>();
            sorts.add(Sort.Order.desc("id"));
            Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));
            return postRepository.findByCommentFromPaging(kw, pageable);
        }

    }
